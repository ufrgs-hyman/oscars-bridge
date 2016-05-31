package br.rnp.meican.oscars;

import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.client.OSCARSClient;
import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.client.OSCARSClientException;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;

public class App {
    public static void main(String[] args){
        try {
            //Setup keystores 
            OSCARSClientConfig.setClientKeystore(
            	"", 
                "/Users/mqg/Documents/workspacejee/cod/oscars.jks", 
                "");
            OSCARSClientConfig.setSSLKeyStore(
        		"/Users/mqg/Documents/workspacejee/cod/localhost.jks", 
        		"");

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

            //print reservations
            for(ResDetails resDetails : reply.getResDetails() ){
                System.out.println("GRI: " + resDetails.getGlobalReservationId());
                System.out.println("Login: " + resDetails.getLogin());
                System.out.println("Status: " + resDetails.getStatus());
                System.out.println("Start Time: " + resDetails.getUserRequestConstraint().getStartTime());
                System.out.println("End Time: " + resDetails.getUserRequestConstraint().getEndTime());
                System.out.println("Bandwidth: " + resDetails.getUserRequestConstraint().getBandwidth());
                System.out.println();
            }
        } catch (OSCARSClientException e) {
            System.err.println("Error configuring client: " + e.getMessage());
            System.exit(1);
        } catch (OSCARSFaultMessage e) {
            System.err.println("Error returned from server: " + e.getMessage());
            System.exit(1);
        }
    }
}