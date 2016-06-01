package br.rnp.meican.oscars;

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

/**
 * @author Maurício Quatrin Guerreiro
 */
public class App {
	
    public static void main(String[] args){
        try {
            //Setup keystores 
            OSCARSClientConfig.setClientKeystore(
            	"gt-ater", 
                "/Users/mqg/Documents/workspacejee/cod/oscars.jks", 
                "changeit");
            OSCARSClientConfig.setSSLKeyStore(
        		"/Users/mqg/Documents/workspacejee/cod/localhost.jks", 
        		"changeit");

            //initialize client with service URL
            OSCARSClient client = new OSCARSClient("https://idc.cipo.rnp.br:9001/OSCARS/");

            //Build request that asks for all ACTIVE and RESERVED reservations
            ListRequest request = new ListRequest();
            request.getResStatus().add(OSCARSClient.STATUS_ACTIVE);
            request.getResStatus().add(OSCARSClient.STATUS_RESERVED);

            //send request
            ListReply reply = client.listReservations(request);

            //handle case where no reservations returned
            if(reply.getResDetails().size() == 0){
                System.out.println("No ACTIVE or RESERVED reservations found.");
                System.exit(0);
            }
            System.out.println("=======START===RESERVATIONS=======");

            //print reservations
            for(ResDetails resDetails : reply.getResDetails() ){
            	System.out.println("=======START===CIRCUIT========");
                System.out.println("GRI: " + resDetails.getGlobalReservationId());
                System.out.println("Login: " + resDetails.getLogin());
                System.out.println("Description: " + resDetails.getDescription());
                System.out.println("Status: " + resDetails.getStatus());
                System.out.println("Start Time: " + resDetails.getUserRequestConstraint().getStartTime());
                System.out.println("End Time: " + resDetails.getUserRequestConstraint().getEndTime());
                System.out.println("Bandwidth: " + resDetails.getUserRequestConstraint().getBandwidth());
                
                System.out.print("Path: ");
                PathInfo pathInfo = null;
        		ReservedConstraintType rConstraint = resDetails.getReservedConstraint();
        		if (rConstraint !=  null) {
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
        						CtrlPlaneSwcapContent swcap = link.
        								getSwitchingCapabilityDescriptors();
        						if (swcap != null) {
        							CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = 
        									swcap.getSwitchingCapabilitySpecificInfo();
        							if (specInfo != null) {
        								vlanRangeAvail = specInfo.
        										getVlanRangeAvailability();
        							}
        						}
        						System.out.print(link.getId() + ":vlan=" + vlanRangeAvail + ';');
        					} else {
        						System.out.println(ctrlHop.getLinkIdRef());
        					}
        				}
        			}
        		} else {
        			System.out.println("pathHops = no path information available");
        		}
        		System.out.println();
        		System.out.println("=======END===CIRCUIT========");
            }
            System.out.println("=======END===RESERVATIONS=======");
        } catch (OSCARSClientException e) {
            System.err.println("Error configuring client: " + e.getMessage());
            System.exit(1);
        } catch (OSCARSFaultMessage e) {
            System.err.println("Error returned from server: " + e.getMessage());
            System.exit(1);
        }
    }
}