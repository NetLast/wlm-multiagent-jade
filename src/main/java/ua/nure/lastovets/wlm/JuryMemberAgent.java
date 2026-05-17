package ua.nure.lastovets.wlm;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetResponder;

public class JuryMemberAgent extends Agent {

    protected void setup() {
        System.out.println("👨‍⚖️ JuryMemberAgent " + getAID().getLocalName() + " запущено.");
        registerInDF();

        addBehaviour(new JuryEvaluationResponder());
    }

    private void registerInDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("jury-member");
        sd.setName("Jury-Evaluator");
        dfd.addServices(sd);
        try { DFService.register(this, dfd); } catch (FIPAException e) { e.printStackTrace(); }
    }

    private class JuryEvaluationResponder extends ContractNetResponder {
        public JuryEvaluationResponder() {
            super(myAgent, null);
        }

        protected ACLMessage handleCfp(ACLMessage cfp) {
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setContent("available|score-time:8");
            return propose;
        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            ACLMessage inform = accept.createReply();
            try {
                Thread.sleep(2500); // імітація оцінювання
                int score = (int) (Math.random() * 40) + 60; // оцінка 60-100
                inform.setPerformative(ACLMessage.INFORM);
                inform.setContent("EVALUATED|score=" + score + "|photo=" + cfp.getContent());
                System.out.println("⭐ " + getAID().getLocalName() + " поставив оцінку: " + score);
            } catch (Exception e) {
                inform.setPerformative(ACLMessage.FAILURE);
            }
            return inform;
        }
    }

    protected void takeDown() {
        try { DFService.deregister(this); } catch (Exception ignored) {}
    }
}
