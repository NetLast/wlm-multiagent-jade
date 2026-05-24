package ua.nure.lastovets.wlm;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

import java.util.Vector;

public class JuryDispatcherAgent extends Agent {

    @Override
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

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    /** Чекає запити на оцінку */
    private class JuryDispatchBehaviour extends CyclicBehaviour {

        @Override
        public void action() {

            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

            if (msg != null) {

                System.out.println("📤 Розподіл фото: " + msg.getContent());

                ACLMessage cfp = prepareJuryCFP(msg);

                addBehaviour(new JuryContractNet(JuryDispatcherAgent.this, cfp));

            } else {
                block();
            }
        }
    }

    /** Формує CFP */
    private ACLMessage prepareJuryCFP(ACLMessage task) {

        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

        cfp.setContent(task.getContent());
        cfp.setConversationId("jury-evaluation-" + System.currentTimeMillis());
        cfp.setReplyByDate(new java.util.Date(System.currentTimeMillis() + 15000));

        cfp.addReceiver(new AID("jury-member-1", AID.ISLOCALNAME));
        cfp.addReceiver(new AID("jury-member-2", AID.ISLOCALNAME));

        return cfp;
    }

    /** Contract Net */
    private class JuryContractNet extends ContractNetInitiator {

        public JuryContractNet(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        @Override
        protected void handleInform(ACLMessage inform) {

            System.out.println("🏆 Оцінка від " +
                    inform.getSender().getLocalName() +
                    " -> " + inform.getContent());
        }

        @Override
        protected void handlePropose(ACLMessage propose, Vector acceptances) {

            ACLMessage accept = propose.createReply();
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

            acceptances.add(accept);
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {

            System.out.println("❌ Відмова від " +
                    refuse.getSender().getLocalName());
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception ignored) {}

        System.out.println("🛑 JuryDispatcherAgent завершено.");
    }
}