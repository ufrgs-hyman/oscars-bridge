package OSCARSBridge;

import net.es.oscars.notify.ws.AAAFaultMessage;
import org.apache.axis2.AxisFault;
import net.es.oscars.client.*;
import net.es.oscars.wsdlTypes.*;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import net.es.oscars.oscars.*;
import org.ogf.schema.network.topology.ctrlplane.*;

/**
 *
 * @author pfbiasuz
 */
public class OSCARSBridge {

    public String createReservation(String oscars_url, String desc,
            String srcUrn, Boolean isSrcTagged, String srcTag,
            String destUrn, Boolean isDestTagged, String destTag,
            String[] hops, int bandwidth, long startTimestamp, long endTimestamp) {

        String repo = "repo";

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

        pathInfo.setPathSetupMode("signal-xml");
        Boolean setPath = true;

        if (!hops[0].equals("null")) {

            /**
             *  pontos origem e destino devem ser colocados completos nos hops, se o ponto de início/fim for algum hop antes, ele configura somente até o hop discriminado
             *  pontos intermediários podem ser definidos parcialmente
             *
             */
//            String[] hops = {
//                "urn:ogf:network:domain=oscars7.ufrgs.br:node=vlsr1:port=4:link=11.3.11.2",
//                "urn:ogf:network:domain=oscars5.ufrgs.br:node=vlsr2",
//                "urn:ogf:network:domain=oscars2.ufrgs.br:node=vlsr1:port=3:link=11.1.8.1"
//            };
            CtrlPlanePathContent path = new CtrlPlanePathContent();
            path.setId("userPath");
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
                    path.addHop(hop);
                }
            }
            if (hasEro) {
                pathInfo.setPath(path);
                //pathInfo.setPathType("0");
            }
        }

        ResCreateContent request = new ResCreateContent();
        request.setBandwidth(bandwidth);
        request.setStartTime(System.currentTimeMillis() / 1000);
        request.setEndTime(System.currentTimeMillis() / 1000 + 60 * 5);
        request.setDescription(desc);
        request.setPathInfo(pathInfo);

        /**
         * nao consegue setar vlans diferentes entre origem e destino. Resulta em FAILED. Na wbui tambem nao.
         * Setando a vlan origem, a vlan destino é a mesma (normalmente)
         *
         */
        VlanTag srcVtag = new VlanTag();
        srcVtag.setTagged(isSrcTagged);
        srcVtag.setString(srcTag);
        layer2Info.setSrcVtag(srcVtag);

        VlanTag destVtag = new VlanTag();
        destVtag.setTagged(isDestTagged);
        destVtag.setString(destTag);
        layer2Info.setDestVtag(destVtag);

        pathInfo.setLayer2Info(layer2Info);


        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
        }

        try {
            CreateReply response = oscarsClient.createReservation(request);
            String gri = response.getGlobalReservationId();
            String status = response.getStatus();

            System.out.println("GRI: " + gri);
            System.out.println("Initial Status: " + status);
            return gri;

        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (AAAFaultMessage e) {
            e.printStackTrace();


        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    public ArrayList<String> queryReservation(String oscars_url, String gri) {
        String repo = "repo";

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
        }
        try {
            GlobalReservationId request = new GlobalReservationId();
            request.setGri(gri);

            ResDetails response = oscarsClient.queryReservation(request);
            PathInfo pathInfo = response.getPathInfo();
            CtrlPlanePathContent path = pathInfo.getPath();
            Layer2Info layer2Info = pathInfo.getLayer2Info();
            Layer3Info layer3Info = pathInfo.getLayer3Info();
            MplsInfo mplsInfo = pathInfo.getMplsInfo();

            //Reservation queried = new Reservation();
            ArrayList<String> queried = new ArrayList();

            System.out.println("GRI: " + response.getGlobalReservationId());
            System.out.println("Description: " + response.getDescription());
            System.out.println("Login: " + response.getLogin());
            System.out.println("Status: " + response.getStatus());
            System.out.println("Time of request: " + response.getCreateTime());
            System.out.println("Start Time: " + response.getStartTime());
            System.out.println("End Time: " + response.getEndTime());
            System.out.println("Bandwidth: " + response.getBandwidth());
            System.out.println("Path Setup Mode: " + pathInfo.getPathSetupMode());

            queried.add(response.getGlobalReservationId());
            queried.add(response.getDescription());
            queried.add(response.getLogin());
            queried.add(response.getStatus());
            queried.add(String.valueOf(response.getCreateTime()));
            queried.add(String.valueOf(response.getStartTime()));
            queried.add(String.valueOf(response.getEndTime()));
            queried.add(String.valueOf(response.getBandwidth()));
            queried.add(pathInfo.getPathSetupMode());


            if (layer2Info != null) {
                System.out.println("Source Endpoint: " + layer2Info.getSrcEndpoint());
                VlanTag srcVtag = new VlanTag();
                srcVtag = layer2Info.getSrcVtag();
                String srcVlan = srcVtag.getString();
                Boolean isTagged = srcVtag.getTagged();
                System.out.println("Is Src tagged: " + isTagged.toString());
                System.out.println("Vlan Src value: " + srcVlan);
                queried.add(layer2Info.getSrcEndpoint());
                queried.add(isTagged.toString());
                queried.add(srcVlan);
                System.out.println("Destination Endpoint: " + layer2Info.getDestEndpoint());
                VlanTag destVtag = new VlanTag();
                destVtag = layer2Info.getDestVtag();
                String destVlan = destVtag.getString();
                Boolean isTaggedDest = destVtag.getTagged();
                System.out.println("Is Dest tagged: " + isTaggedDest.toString());
                System.out.println("Vlan Dest value: " + destVlan);
                queried.add(layer2Info.getDestEndpoint());
                queried.add(isTaggedDest.toString());
                queried.add(destVlan);

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
            String output = "";

            for (CtrlPlaneHopContent hop : path.getHop()) {
                CtrlPlaneLinkContent link = hop.getLink();
                if (link == null) {
                    //should not happen
                    output += "no link";
                    continue;
                }
                output += "\t" + link.getId();
                queried.add(link.getId());
                //CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
                //CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = swcap.getSwitchingCapabilitySpecificInfo();
                //output += ", " + swcap.getEncodingType();
//                if ("ethernet".equals(swcap.getEncodingType())) {
//                    output += ", " + swcapInfo.getVlanRangeAvailability();
//                }
                output += "\n";
            }
            System.out.println(output);
            return queried;

        } catch (AxisFault e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AAAFaultMessage e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean cancelReservation(String oscars_url, String gri) {

        String repo = "repo";

        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, oscars_url, repo);
        } catch (AxisFault e) {
            e.printStackTrace();
        }

        GlobalReservationId rt = new GlobalReservationId();

        rt.setGri(gri);
        try {
            String response = oscarsClient.cancelReservation(rt);
            return true;

        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (AAAFaultMessage e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}