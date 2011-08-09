/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package OSCARSBridge;

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
        String[] retorno;



//        long upini = System.currentTimeMillis() / 1000;
//        long upfim = (System.currentTimeMillis() + 3600) / 1000;
//
//        String inicio = String.valueOf(upini);
//        String fim = String.valueOf(upfim);
//
//
//        String[] ArrStg = {
//        "http://200.132.1.28:8080/axis2/services/OSCARS",
//        "urn:ogf:network:domain=ufsc.cipo.rnp.br:node=UFSC-CIPO-RNP-001:port=6:link=*",
//        "urn:ogf:network:domain=ufsc.cipo.rnp.br:node=UFSC-CIPO-RNP-002:port=6:link=*",
//        "100",
//        inicio,
//        fim,
//        "urn:ogf:network:domain=ufsc.cipo.rnp.br:node=UFSC-CIPO-RNP-001:port=8:link=*",
//        "urn:ogf:network:domain=ufsc.cipo.rnp.br:node=UFSC-CIPO-RNP-002:port=2:link=*",
//        "100",
//        inicio,
//        fim
//        };
//
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
        teste.getTopology();

    }

}