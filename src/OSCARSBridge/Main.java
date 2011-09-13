/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package OSCARSBridge;

import java.util.ArrayList;
import java.util.Locale;

/**
 *
 * @author pfbiasuz
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DesktopClient teste = new DesktopClient();

        ArrayList<String> resultArray = new ArrayList();

        String oscars_url = "http://oscars.cipo.rnp.br:8080/axis2/services/OSCARS";

        long upStart = System.currentTimeMillis() / 1000;
        long upFinish = (System.currentTimeMillis() + 3600) / 1000;

        String start = String.valueOf(upStart);
        String finish = String.valueOf(upFinish);

        String src = "urn:ogf:network:domain=cipo.rnp.br:node=RJO:port=ge-2/3/4:link=10.0.2.1";
        String dest = "urn:ogf:network:domain=cipo.rnp.br:node=SPO:port=ge-2/3/4:link=10.0.4.1";

        int bandwidth = 200;
        
        String isSrcTagged = "false";
        String srcTag = "any";
        String isDestTagged = "false";
        String destTag = "700";

        resultArray = teste.createReservation(oscars_url, "Reserva Teste", src, isSrcTagged, srcTag, dest, isDestTagged, destTag, "null", bandwidth, "timer-automatic", upStart, upFinish);

        System.out.println(resultArray.toString());

//        retorno = teste.create(ArrStg);
//        for (int ind=0; ind < retorno.length; ind++){
//            System.out.println(retorno[ind]);
//        }

//        String[] gris = {
//            "http://200.132.1.28:8080/axis2/services/OSCARS",
//            "ufrgs.cipo.rnp.br-1259",
//            "ufrgs.cipo.rnp.br-1086",
//            "ufrgs.cipo.rnp.br-1897"
//        };
//
//        retorno = teste.query(gris);
//        for (int ind=0; ind < retorno.length; ind++){
//            System.out.println(retorno[ind]);
//        }
    String res = "oscars7.ufrgs.br-84";

        //teste.createTeste();
        //teste.cancelTeste(res);
        //teste.queryAll(res);
        //teste.createPath(res);
        //teste.teardownPath(res);
        //teste.refreshPath(res);
        //teste.modifyReservation(res);
        //teste.getTopology();

    }

}