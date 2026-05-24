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

import java.util.Date;
import java.util.Vector;

public class CoordinatorAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("✅ CoordinatorAgent " + getAID().getLocalName() + " запущено.");

        registerInDF();

        addBehaviour(new TaskReceiverBehaviour());
    }

    private void registerInDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("photo-coordinator");
        sd.setName("WLM-Coordinator");

        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("📋 Coordinator зареєстровано в DF");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    /** Чекає вхідні запити */
    private class TaskReceiverBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

            if (msg != null) {
                System.out.println("📸 Отримано фото: " + msg.getContent());

                addBehaviour(new TechValidationContractNet(CoordinatorAgent.this, msg));

            } else {
                block();
            }
        }
    }

    /** Contract Net */
    private class TechValidationContractNet extends ContractNetInitiator {

        public TechValidationContractNet(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        @Override
        protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {

            Vector<ACLMessage> cfps = new Vector<>();

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("tech-validator");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(CoordinatorAgent.this, template);

                if (result.length == 0) {
                    System.out.println("⚠️ TechValidatorAgent не знайдено");
                    return cfps;
                }

                ACLMessage base = (ACLMessage) cfp.clone();
                base.setPerformative(ACLMessage.CFP);
                base.setConversationId("tech-check-" + System.currentTimeMillis());
                base.setReplyByDate(new Date(System.currentTimeMillis() + 15000));

                for (DFAgentDescription dfd : result) {
                    ACLMessage msg = (ACLMessage) base.clone();
                    msg.addReceiver(dfd.getName());
                    cfps.add(msg);

                    System.out.println("📤 CFP -> " + dfd.getName().getLocalName());
                }

            } catch (FIPAException e) {
                e.printStackTrace();
            }

            return cfps;
        }

        @Override
        protected void handlePropose(ACLMessage propose, Vector acceptances) {

            ACLMessage accept = propose.createReply();
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            acceptances.add(accept);

            System.out.println("✅ PROPOSE від " + propose.getSender().getLocalName());
        }

        @Override
        protected void handleInform(ACLMessage inform) {

            System.out.println("✅ OK від " + inform.getSender().getLocalName()
                    + " -> " + inform.getContent());
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {

            System.out.println("❌ REFUSE від " + refuse.getSender().getLocalName());
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception ignored) {}

        System.out.println("🛑 CoordinatorAgent завершено.");
    }
}