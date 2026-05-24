package ua.nure.lastovets.wlm;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

public class TechValidatorAgent extends Agent {

    protected void setup() {
        System.out.println("🔧 TechValidatorAgent " + getAID().getLocalName() + " запущено.");
        registerInDF();

        addBehaviour(new TechValidationResponder());
    }

    private void registerInDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("tech-validator");
        sd.setName("Technical-Photo-Validator");
        dfd.addServices(sd);
        try { DFService.register(this, dfd); } catch (FIPAException e) { e.printStackTrace(); }
    }

    private class TechValidationResponder extends ContractNetResponder {
        public TechValidationResponder() {
            super(TechValidatorAgent.this, MessageTemplate.MatchPerformative(ACLMessage.CFP));
        }

        protected ACLMessage handleCfp(ACLMessage cfp) {
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setContent("ready|" + System.currentTimeMillis());
            return propose;
        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);

            try {
                // Симуляція перевірки зображення
                Thread.sleep(800); // імітація обробки
                inform.setContent("TECH_OK: " + cfp.getContent());
                System.out.println("✅ " + getAID().getLocalName() + " пройшов технічну перевірку");
            } catch (Exception e) {
                inform.setPerformative(ACLMessage.FAILURE);
                inform.setContent("TECH_FAILED");
            }
            return inform;
        }
    }

    protected void takeDown() {
        try { DFService.deregister(this); } catch (Exception ignored) {}
    }
}
