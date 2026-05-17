package ua.nure.lastovets.wlm;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.util.Vector;

public class JuryDispatcherAgent extends Agent {

    protected void setup() {
        System.out.println("🏛️ JuryDispatcherAgent " + getAID().getLocalName() + " запущено.");
        registerInDF();

        addBehaviour(new JuryDispatchBehaviour());
    }

    private void registerInDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("jury-dispatcher");
        sd.setName("Jury-Dispatcher");
        dfd.addServices(sd);
        try { DFService.register(this, dfd); } catch (FIPAException e) { e.printStackTrace(); }
    }

    private class JuryDispatchBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                System.out.println("📤 Диспетчер розподіляє фото для журі: " + msg.getContent());
                addBehaviour(new JuryContractNet(myAgent, prepareJuryCFP(msg)));
            } else {
                block();
            }
        }
    }

    private ACLMessage prepareJuryCFP(ACLMessage task) {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.setContent(task.getContent());
        cfp.setConversationId("jury-evaluation-" + System.currentTimeMillis());
        cfp.setReplyByDate(new java.util.Date(System.currentTimeMillis() + 15000));
        // Додаємо відомих членів журі
        cfp.addReceiver(new AID("jury-member-1", AID.ISLOCALNAME));
        cfp.addReceiver(new AID("jury-member-2", AID.ISLOCALNAME));
        return cfp;
    }

    private class JuryContractNet extends ContractNetInitiator {
        public JuryContractNet(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println("🏆 Оцінка отримана від журі: " + inform.getSender().getLocalName());
        }
    }

    protected void takeDown() {
        try { DFService.deregister(this); } catch (Exception ignored) {}
    }
}
