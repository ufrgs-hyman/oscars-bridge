package br.rnp.meican.oscars.service;

import java.util.ArrayList;
import java.util.List;

import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.client.OSCARSClient;
import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.client.OSCARSClientException;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import br.rnp.meican.oscars.domain.Circuit;

/**
 * @author Maurício Quatrin Guerreiro
 */
@Service
@Scope("singleton")
public class BridgeService {

    private String providerUrl; 

    /**
     * Build a service based on application arguments.
     * 
     * url - OSCARS url
     * ok - OSCARS Keystore 
     * ou - OSCARS Keystore user 
     * op - OSCARS Keystore password 
     * lk - localhost Keystore 
     * lp - localhost Keystore password
     */
    @Autowired
    public BridgeService(Environment env) {
        this.configure(env.getProperty("url"), env.getProperty("ok"),
                env.getProperty("ou"), env.getProperty("op"),
                env.getProperty("lk"), env.getProperty("lp"));
    }

    public void configure(String providerUrl, String clientKeystore,
            String clientUser, String clientPass, String serverKeystore,
            String serverPass) {
        this.providerUrl = providerUrl;
        // Setup keystores
        try {
            OSCARSClientConfig.setClientKeystore(clientUser, clientKeystore,
                    clientPass);
            OSCARSClientConfig.setSSLKeyStore(serverKeystore, serverPass);
        } catch (OSCARSClientException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Circuit> getCircuits() {
        ArrayList<Circuit> circuits = new ArrayList<Circuit>();
        try {
            // initialize client with service URL
            OSCARSClient client = new OSCARSClient(this.providerUrl);

            // Build request that asks for all ACTIVE and RESERVED reservations
            ListRequest request = new ListRequest();
            request.getResStatus().add(OSCARSClient.STATUS_ACTIVE);
            request.getResStatus().add(OSCARSClient.STATUS_RESERVED);

            // send request
            ListReply reply = client.listReservations(request);

            if (reply.getResDetails().size() == 0)
                return circuits;

            // print reservations
            for (ResDetails resDetails : reply.getResDetails()) {
                Circuit circuit = new Circuit();
                circuits.add(circuit);
                circuit.setGri(resDetails.getGlobalReservationId());
                circuit.setUser(resDetails.getLogin());
                circuit.setDescription(resDetails.getDescription());
                circuit.setStatus(resDetails.getStatus());
                circuit.setStartTime(resDetails.getUserRequestConstraint()
                        .getStartTime());
                circuit.setEndTime(resDetails.getUserRequestConstraint()
                        .getEndTime());
                circuit.setBandwidth(resDetails.getUserRequestConstraint()
                        .getBandwidth());

                PathInfo pathInfo = null;
                ReservedConstraintType rConstraint = resDetails
                        .getReservedConstraint();
                if (rConstraint != null) {
                    pathInfo = rConstraint.getPathInfo();
                }

                CtrlPlanePathContent path = pathInfo.getPath();
                if (path != null) {
                    List<CtrlPlaneHopContent> hops = path.getHop();
                    if (hops.size() > 0) {
                        for (CtrlPlaneHopContent ctrlHop : hops) {
                            CtrlPlaneLinkContent link = ctrlHop.getLink();
                            String vlanRangeAvail = "any";
                            if (link != null) {
                                CtrlPlaneSwcapContent swcap = link
                                        .getSwitchingCapabilityDescriptors();
                                if (swcap != null) {
                                    CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = swcap
                                            .getSwitchingCapabilitySpecificInfo();
                                    if (specInfo != null) {
                                        vlanRangeAvail = specInfo
                                                .getVlanRangeAvailability();
                                    }
                                }
                                circuit.addPoint(link.getId() + ":vlan="
                                        + vlanRangeAvail);
                            } else {
                                System.out.println(ctrlHop.getLinkIdRef());
                            }
                        }
                    }
                }
            }
        } catch (OSCARSClientException e) {
            System.err.println("Error configuring client: " + e.getMessage());
        } catch (OSCARSFaultMessage e) {
            System.err.println("Error returned from server: " + e.getMessage());
        }
        return circuits;
    }
}
