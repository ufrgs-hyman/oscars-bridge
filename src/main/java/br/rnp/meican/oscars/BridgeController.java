package br.rnp.meican.oscars;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.rnp.meican.oscars.domain.Circuit;
import br.rnp.meican.oscars.service.BridgeService;

/**
 * @author Maurício Quatrin Guerreiro
 */
@RestController
public class BridgeController {
	
	@Autowired
	private BridgeService service;

    @RequestMapping("/oscars-bridge/circuits")
    public ArrayList<Circuit> getCircuits() {
        return service.getCircuits();
    }
}
