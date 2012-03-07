package OSCARSBridge;

import net.es.oscars.notify.ws.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;

import org.apache.axis2.AxisFault;
import net.es.oscars.client.Client;
import net.es.oscars.wsdlTypes.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import org.ogf.schema.network.topology.ctrlplane.*;

/**
 *
 * @author pfbiasuz
 */
public class OSCARSBridge {
    
    public static String repoDir = "repo";
    
    /*public void printAbsolutePath() {
        File repo_file = new File(repoDir);
        System.out.println("--Diretorio absoluto: " + repo_file.getAbsolutePath());
    }*/

    public ArrayList<String> createReservation(String oscars_url, String description,
            String srcUrn, String isSrcTagged, String srcTag,
            String destUrn, String isDestTagged, String destTag,
            String path, int bandwidth, String pathSetupMode,
            long startTimestamp, long endTimestamp) {

        String repo = repoDir;
        ArrayList<String> retorno = new ArrayList();
        String message;

        /**
         * pathSetupMode (string)
        "timer-automatic" means that the reserved circuit will be instantiated by the scheduler process
        "signal-xml" means the user will signal to instantiate the reserved circuit
         * aceita qualquer string no path setup mode
         * timer-automatic dispara a reserva na hora certa
         * strings invalidas nao disparam, ficam em pending como se fosse signal-xml, nao gera TOKEN
         */
        Client oscarsClient = new Client();

        /**
         * ResCreateContent
         *      PathInfo
         *          layer2Info
         *
         */
        Layer2Info layer2Info = new Layer2Info();
        layer2Info.setSrcEndpoint(srcUrn);
        layer2Info.setDestEndpoint(destUrn);

        PathInfo pathInfo = new PathInfo();

        pathInfo.setPathSetupMode(pathSetupMode);
        Boolean setPath = true;

        if (!path.equals("null")) {
            String[] hops = path.split(";");
            /**
             *  pontos origem e destino devem ser colocados completos nos hops,
             *  se o ponto de início/fim for algum hop antes, ele configura
             *  somente até o hop discriminado pontos intermediários podem ser
             *  definidos parcialmente
             *
             */
            CtrlPlanePathContent pathContent = new CtrlPlanePathContent();
            pathContent.setId("userPath");
            boolean hasEro = true;

            for (int i = 0; i < hops.length; i++) {
                String propName = "ero_" + Integer.toString(i);
                String hopId = hops[i];
                if (hopId != null) {
                    hopId = hopId.trim();
                    int hopType = hopId.split(":").length;
                    hasEro = true;
                    CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
                    hop.setId(i + "");
                    if (hopType == 4) {
                        hop.setDomainIdRef(hopId);
                    } else if (hopType == 5) {
                        hop.setNodeIdRef(hopId);
                    } else if (hopType == 6) {
                        hop.setPortIdRef(hopId);
                    } else {
                        hop.setLinkIdRef(hopId);
                    }
                    pathContent.addHop(hop);
                }
            }
            if (hasEro) {
                pathInfo.setPath(pathContent);
                //pathInfo.setPathType("0");
            }
        }

        ResCreateContent request = new ResCreateContent();
        request.setBandwidth(bandwidth);
        request.setStartTime(startTimestamp);
        request.setEndTime(endTimestamp);
        request.setDescription(description);
        request.setPathInfo(pathInfo);

        VlanTag srcVtag = new VlanTag();

        if (isSrcTagged.equals("true")) {
            srcVtag.setTagged(true);
            srcVtag.setString(srcTag);
        } else {
            srcVtag.setTagged(false);
            srcVtag.setString("0");
        }
        layer2Info.setSrcVtag(srcVtag);

        VlanTag destVtag = new VlanTag();

        if (isDestTagged.equals("true")) {
            destVtag.setTagged(true);
            destVtag.setString(destTag);
        } else {
            destVtag.setTagged(false);
            destVtag.setString("0");
        }
        layer2Info.setDestVtag(destVtag);

        pathInfo.setLayer2Info(layer2Info);


        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AxisFault (" + message + ")");
            return retorno;
        }

        try {
            CreateReply response = oscarsClient.createReservation(request);
            String gri = response.getGlobalReservationId();
            String status = response.getStatus();

            System.out.println("GRI: " + gri);
            System.out.println("Initial Status: " + status);


            retorno.add(0, "");
            retorno.add(gri);
            retorno.add(status);

        } catch (RemoteException e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: RemoteException (" + message + ")");
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AAAFaultMessage (" + message + ")");
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: Exception (" + message + ")");
        }
        oscarsClient.cleanUp();
        return retorno;
    }

    public ArrayList<String> queryReservation(String oscars_url, String gri) {
        String repo = repoDir;

        ArrayList<String> retorno = new ArrayList();
        String message;

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AxisFault (" + message + ")");
            return retorno;
        }


        try {
            GlobalReservationId request = new GlobalReservationId();
            request.setGri(gri);

            ResDetails response = oscarsClient.queryReservation(request);
            
            PathInfo pathInfo = response.getPathInfo();
            CtrlPlanePathContent path = pathInfo.getPath();
            Layer2Info layer2Info = pathInfo.getLayer2Info();
//            Layer3Info layer3Info = pathInfo.getLayer3Info();
//            MplsInfo mplsInfo = pathInfo.getMplsInfo();


            System.out.println("GRI: " + response.getGlobalReservationId());
            System.out.println("Status: " + response.getStatus());
            System.out.println("Description: " + response.getDescription());
            System.out.println("Login: " + response.getLogin());

            System.out.println("Time of request: " + response.getCreateTime());
            System.out.println("Start Time: " + response.getStartTime());
            System.out.println("End Time: " + response.getEndTime());
            System.out.println("Bandwidth: " + response.getBandwidth());
            System.out.println("Path Setup Mode: " + pathInfo.getPathSetupMode());

            retorno.add(response.getGlobalReservationId());
            retorno.add(response.getStatus());
            retorno.add(response.getDescription());
            retorno.add(response.getLogin());

            retorno.add(String.valueOf(response.getCreateTime()));
            retorno.add(String.valueOf(response.getStartTime()));
            retorno.add(String.valueOf(response.getEndTime()));
            retorno.add(String.valueOf(response.getBandwidth()));
            retorno.add(pathInfo.getPathSetupMode());


            if (layer2Info != null) {
                System.out.println("Source Endpoint: " + layer2Info.getSrcEndpoint());
                VlanTag srcVtag = new VlanTag();
                srcVtag = layer2Info.getSrcVtag();
                String srcVlan = srcVtag.getString();
                Boolean isTagged = srcVtag.getTagged();
                System.out.println("Is Src tagged: " + isTagged.toString());
                System.out.println("Vlan Src value: " + srcVlan);
                retorno.add(layer2Info.getSrcEndpoint());
                retorno.add(isTagged.toString());
                retorno.add(srcVlan);
                System.out.println("Destination Endpoint: " + layer2Info.getDestEndpoint());
                VlanTag destVtag = new VlanTag();
                destVtag = layer2Info.getDestVtag();
                String destVlan = destVtag.getString();
                Boolean isTaggedDest = destVtag.getTagged();
                System.out.println("Is Dest tagged: " + isTaggedDest.toString());
                System.out.println("Vlan Dest value: " + destVlan);
                retorno.add(layer2Info.getDestEndpoint());
                retorno.add(isTaggedDest.toString());
                retorno.add(destVlan);

            }
//            if (layer3Info != null) {
//                System.out.println("Source Host: " + layer3Info.getSrcHost());
//                System.out.println("Destination Host: " + layer3Info.getDestHost());
//                System.out.println("Source L4 Port: " + layer3Info.getSrcIpPort());
//                System.out.println("Destination L4 Port: " + layer3Info.getDestIpPort());
//                System.out.println("Protocol: " + layer3Info.getProtocol());
//                System.out.println("DSCP: " + layer3Info.getDscp());
//            }
//            if (mplsInfo != null) {
//                System.out.println("Burst Limit: " + mplsInfo.getBurstLimit());
//                System.out.println("LSP Class: " + mplsInfo.getLspClass());
//            }


            System.out.println("Path: ");
            StringBuilder pathString = new StringBuilder();

            for (CtrlPlaneHopContent hop : path.getHop()) {
                CtrlPlaneLinkContent link = hop.getLink();
                if (link == null) {
                    //should not happen
                    pathString.append("no link");
                    pathString.append(";");
                    continue;
                }
                pathString.append(link.getId());
                pathString.append(";");
                //CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
                //CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = swcap.getSwitchingCapabilitySpecificInfo();
                //output += ", " + swcap.getEncodingType();
//                if ("ethernet".equals(swcap.getEncodingType())) {
//                    output += ", " + swcapInfo.getVlanRangeAvailability();
//                }
            }
            System.out.println(pathString.toString());
            retorno.add(pathString.toString());

            retorno.add(0, "");

        } catch (RemoteException e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: RemoteException (" + message + ")");
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AAAFaultMessage (" + message + ")");
        } catch (BSSFaultMessage e) {
            e.printStackTrace();
            message = e.getFaultMessage().getMsg();
            System.out.println("Error: BSSFaultMessage (" + message + ")");
            retorno.add(0, "Error: BSSFaultMessage (" + message + ")");
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: Exception (" + message + ")");
        }
        oscarsClient.cleanUp();
        return retorno;
    }

    public ArrayList<String> cancelReservation(String oscars_url, String gri) {

        String repo = repoDir;
        ArrayList<String> retorno = new ArrayList();
        String message;

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AxisFault (" + message + ")");
            return retorno;
        }

        GlobalReservationId rt = new GlobalReservationId();

        rt.setGri(gri);

        try {
            String status = oscarsClient.cancelReservation(rt);
            System.out.println("Global Reservation Id: " + gri);
            System.out.println("Cancel Status: " + status);
            retorno.add(gri);
            retorno.add(status);
            retorno.add(0, "");

        } catch (RemoteException e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: RemoteException (" + message + ")");
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AAAFaultMessage (" + message + ")");
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: Exception (" + message + ")");
        }
        oscarsClient.cleanUp();
        return retorno;
    }

    public ArrayList<String> listReservations(String oscars_url, String grisString) {
        String repo = repoDir;
        String message;
        ArrayList<String> retorno = new ArrayList();

        String[] gris = grisString.split(";");

        Client oscarsClient = new Client();

        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AxisFault (" + message + ")");
            return retorno;
        }

        ResDetails response = new ResDetails();

        GlobalReservationId rt = new GlobalReservationId();

        String temp;

        for (int ind = 0; ind < gris.length; ind++) {

            rt.setGri(gris[ind]);

            try {
                response = oscarsClient.queryReservation(rt);
                temp = response.getStatus();
            } catch (RemoteException e) {
                e.printStackTrace();
                message = e.getMessage();
                temp = "Error: RemoteException (" + message + ")";
            } catch (AAAFaultMessage e) {
                e.printStackTrace();
                message = e.getMessage();
                temp = "Error: AAAFaultMessage (" + message + ")";
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
                temp = "Error: Exception (" + message + ")";
            }
            System.out.println(temp);
            retorno.add(temp);
        }
        retorno.add(0, "");
        oscarsClient.cleanUp();
        return retorno;
    }

    public ArrayList<String> modifyReservation(String oscars_url, String gri, long startTimestamp, long endTimestamp) {
        String repo = repoDir;
        String message;
        ArrayList<String> retorno = new ArrayList();

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AxisFault (" + message + ")");
            return retorno;
        }

        try {
            ModifyResContent content = new ModifyResContent();

            content.setGlobalReservationId(gri);
            content.setStartTime(startTimestamp);
            content.setEndTime(endTimestamp);

            //PARAMETROS INUTEIS
            content.setBandwidth(100);
            content.setDescription("nao sera alterada");

            ModifyResReply response = oscarsClient.modifyReservation(content);
            ResDetails reservation = response.getReservation();

            System.out.println("Response:");
            System.out.println("GRI: " + reservation.getGlobalReservationId());
            System.out.println("Status: " + reservation.getStatus().toString());
            retorno.add(0, "");
            retorno.add(reservation.getGlobalReservationId());
            retorno.add(reservation.getStatus().toString());

        } catch (RemoteException e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: RemoteException (" + message + ")");
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: AAAFaultMessage (" + message + ")");
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: Exception (" + message + ")");
        }
        oscarsClient.cleanUp();
        return retorno;
    }

    public ArrayList<String> createPath(String oscars_url, String gri) {

        String repo = repoDir;
        String message;
        ArrayList<String> retorno = new ArrayList();

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AxisFault (" + message + ")");
            return retorno;
        }

        CreatePathContent createRequest = new CreatePathContent();
        createRequest.setGlobalReservationId(gri);
        try {
            CreatePathResponseContent createResponse = oscarsClient.createPath(createRequest);
            System.out.println("Global Reservation Id: " + createResponse.getGlobalReservationId());
            System.out.println("Create Status: " + createResponse.getStatus());
            retorno.add(0, "");
            retorno.add(createResponse.getGlobalReservationId());
            retorno.add(createResponse.getStatus());

        } catch (RemoteException e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: RemoteException (" + message + ")");
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: AAAFaultMessage (" + message + ")");
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: Exception (" + message + ")");
        }
        oscarsClient.cleanUp();
        return retorno;
    }

    public ArrayList<String> teardownPath(String oscars_url, String gri) {
        String repo = repoDir;
        String message;
        ArrayList<String> retorno = new ArrayList();

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AxisFault (" + message + ")");
            return retorno;
        }

        try {
            TeardownPathContent teardownRequest = new TeardownPathContent();
            teardownRequest.setGlobalReservationId(gri);
            TeardownPathResponseContent teardownResponse = oscarsClient.teardownPath(teardownRequest);
            System.out.println("Global Reservation Id: " + teardownResponse.getGlobalReservationId());
            System.out.println("Teardown Status: " + teardownResponse.getStatus());
            retorno.add(0, "");
            retorno.add(teardownResponse.getGlobalReservationId());
            retorno.add(teardownResponse.getStatus());

        } catch (RemoteException e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: RemoteException (" + message + ")");
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: AAAFaultMessage (" + message + ")");
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: Exception (" + message + ")");
        }
        oscarsClient.cleanUp();
        return retorno;
    }

    public ArrayList<String> refreshPath(String oscars_url, String gri) {
        String repo = repoDir;
        String message;
        ArrayList<String> retorno = new ArrayList();

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AxisFault (" + message + ")");
            return retorno;
        }

        try {
            RefreshPathContent refreshRequest = new RefreshPathContent();
            refreshRequest.setGlobalReservationId(gri);
            RefreshPathResponseContent refreshResponse = oscarsClient.refreshPath(refreshRequest);
            System.out.println("Global Reservation Id: " + refreshResponse.getGlobalReservationId());
            System.out.println("Refresh Status: " + refreshResponse.getStatus());
            retorno.add(0, "");
            retorno.add(refreshResponse.getGlobalReservationId());
            retorno.add(refreshResponse.getStatus());

        } catch (RemoteException e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: RemoteException (" + message + ")");
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: AAAFaultMessage (" + message + ")");
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: Exception (" + message + ")");
        }
        oscarsClient.cleanUp();
        return retorno;
    }

    public ArrayList<String> getTopology(String oscars_url) {
        String repo = repoDir;
        String message;
        ArrayList<String> retorno = new ArrayList();
        String temp;

        Client oscarsClient;
        try {
            oscarsClient = new Client();
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0, "Error: AxisFault (" + message + ")");
            return retorno;
        }

        try {
            GetTopologyContent request = new GetTopologyContent();
            request.setTopologyType("all");
            GetTopologyResponseContent response = oscarsClient.getNetworkTopology(request);
            CtrlPlaneDomainContent[] domains = response.getTopology().getDomain();

            for (CtrlPlaneDomainContent d : domains) {
                temp = d.getId();
                retorno.add(temp);
                System.out.println(temp);
                CtrlPlaneNodeContent[] nodes = d.getNode();
                for (CtrlPlaneNodeContent n : nodes) {
                    temp = n.getId();
                    retorno.add(temp);
                    System.out.println(temp);
                    CtrlPlanePortContent[] ports = n.getPort();
                    for (CtrlPlanePortContent p : ports) {
                        temp = p.getId()+" " +p.getCapacity()+" "+p.getGranularity()+" "+p.getMaximumReservableCapacity()+" "+p.getMaximumReservableCapacity();
                        retorno.add(temp);
                        System.out.println(temp);
                        CtrlPlaneLinkContent[] links = p.getLink();
                        if (links != null) {
                            for (CtrlPlaneLinkContent l : links) {
                                CtrlPlaneSwcapContent swcap = l.getSwitchingCapabilityDescriptors();
                                CtrlPlaneSwitchingCapabilitySpecificInfo swcapEsp = swcap.getSwitchingCapabilitySpecificInfo();
                                temp = l.getId()+" "+l.getRemoteLinkId()+" "+l.getCapacity()+ " "+l.getGranularity()+ " "+l.getMinimumReservableCapacity()+" "+l.getMaximumReservableCapacity()+" "+swcapEsp.getVlanRangeAvailability();
                                retorno.add(temp);
                                System.out.println(temp);
                            }
                        }
                    }
                }
            }
            retorno.add(0,"");
        } catch (RemoteException e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: RemoteException (" + message + ")");
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: AAAFaultMessage (" + message + ")");
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            retorno.add(0,"Error: Exception (" + message + ")");
        }
        oscarsClient.cleanUp();
        return retorno;
    }
    
    public ArrayList<String> listAllReservations(String oscars_url, String status) {
        String repo = repoDir;
        
        ArrayList<String> retorno = new ArrayList();
        
        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            System.out.println("AxisFault: " + e.getMessage());
        }
        
        try {
            ListRequest request = new ListRequest();
            request.addResStatus(status);
            ListReply reply = oscarsClient.listReservations(request);
            ResDetails[] details = reply.getResDetails();
            int numFilteredResults = 0;
            
            for (ResDetails detail : details) {
                PathInfo pathInfo = detail.getPathInfo();
                CtrlPlanePathContent path = pathInfo.getPath();
                Layer2Info layer2Info = pathInfo.getLayer2Info();
                Layer3Info layer3Info = pathInfo.getLayer3Info();
                MplsInfo mplsInfo = pathInfo.getMplsInfo();

                String output = "";
                String startTime = String.valueOf(detail.getStartTime());
                String endTime = String.valueOf(detail.getEndTime());
                String bandwidth = String.valueOf(detail.getBandwidth());

                retorno.add(detail.getGlobalReservationId());
                retorno.add(startTime);
                retorno.add(endTime);
                retorno.add(bandwidth);
                retorno.add(detail.getDescription());

                if (layer2Info != null) {
                    String srcVlan = String.valueOf((layer2Info.getSrcVtag()));
                    String destVlan = String.valueOf(layer2Info.getDestVtag());

                    retorno.add(layer2Info.getSrcEndpoint());
                    retorno.add(layer2Info.getDestEndpoint());
                    retorno.add(srcVlan);
                    retorno.add(destVlan);
                }
                if (layer3Info != null) {
                    String srcPort = String.valueOf(layer3Info.getSrcIpPort());
                    String destPort = String.valueOf(layer3Info.getDestIpPort());

                    retorno.add(layer3Info.getSrcHost());
                    retorno.add(layer3Info.getDestHost());
                    retorno.add(srcPort);
                    retorno.add(destPort);
                }
                if (mplsInfo != null) {
                    String burstLimit = String.valueOf(mplsInfo.getBurstLimit());

                    retorno.add(burstLimit);
                    retorno.add(mplsInfo.getLspClass());
                }
            }

        } catch (AxisFault e) {
            System.out.println("AxisFault: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Remote Exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        oscarsClient.cleanUp();
        return retorno;
    }
    
}

