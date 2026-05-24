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

public class TopicValidatorAgent extends Agent {

    protected void setup() {
        System.out.println("📍 TopicValidatorAgent " + getAID().getLocalName() + " запущено.");
        registerInDF();

        addBehaviour(new TopicValidationResponder());
    }

    private void registerInDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("topic-validator");
        sd.setName("Topic-Photo-Validator");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private class TopicValidationResponder extends ContractNetResponder {
        public TopicValidationResponder() {
            super(TopicValidatorAgent.this, MessageTemplate.MatchPerformative(ACLMessage.CFP));
        }

        protected ACLMessage handleCfp(ACLMessage cfp) {
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setContent("topic-ready|" + System.currentTimeMillis());
            return propose;
        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            ACLMessage inform = accept.createReply();
            try {
                Thread.sleep(1200); // імітація аналізу зображення
                inform.setPerformative(ACLMessage.INFORM);
                inform.setContent("TOPIC_OK: Відповідність пам'ятці підтверджено - " + cfp.getContent());
                System.out.println("✅ " + getAID().getLocalName() + " завершив тематичну перевірку");
            } catch (Exception e) {
                inform.setPerformative(ACLMessage.FAILURE);
                inform.setContent("TOPIC_FAILED");
            }
            return inform;
        }
    }

    protected void takeDown() {
        try { DFService.deregister(this); } catch (Exception ignored) {}
    }
}
