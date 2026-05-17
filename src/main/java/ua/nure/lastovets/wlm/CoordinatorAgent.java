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

import java.util.Date;
import java.util.Vector;

public class CoordinatorAgent extends Agent {

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
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private class TaskReceiverBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                System.out.println("📸 Отримана нова фотографія: " + msg.getContent());
                addBehaviour(new PhotoProcessingContractNet(myAgent, msg));
            } else {
                block();
            }
        }
    }

    private class PhotoProcessingContractNet extends ContractNetInitiator {
        public PhotoProcessingContractNet(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {
            Vector<ACLMessage> v = new Vector<>();
            // В реальному проєкті шукати через DF
            ACLMessage cfpCopy = (ACLMessage) cfp.clone();
            cfpCopy.addReceiver(new AID("tech-validator-1", AID.ISLOCALNAME));
            cfpCopy.addReceiver(new AID("tech-validator-2", AID.ISLOCALNAME));
            cfpCopy.addReceiver(new AID("tech-validator-3", AID.ISLOCALNAME));
            v.add(cfpCopy);
            return v;
        }

        protected void handlePropose(ACLMessage propose, Vector acceptances) {
            ACLMessage accept = propose.createReply();
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            acceptances.add(accept);
            System.out.println("✅ Прийнято пропозицію від " + propose.getSender().getLocalName());
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println("✅ Технічна перевірка пройдена: " + inform.getContent());
            // Тут можна запустити наступний етап (TopicValidator)
        }
    }

    protected void takeDown() {
        try { DFService.deregister(this); } catch (Exception ignored) {}
        System.out.println("🛑 CoordinatorAgent завершено.");
    }
}
