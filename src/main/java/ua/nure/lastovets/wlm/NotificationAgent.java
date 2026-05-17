package ua.nure.lastovets.wlm;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class NotificationAgent extends Agent {

    protected void setup() {
        System.out.println("📧 NotificationAgent " + getAID().getLocalName() + " запущено.");
        registerInDF();

        addBehaviour(new NotificationBehaviour());
    }

    private void registerInDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("notification-service");
        sd.setName("WLM-Notifier");
        dfd.addServices(sd);
        try { DFService.register(this, dfd); } catch (FIPAException e) { e.printStackTrace(); }
    }

    private class NotificationBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                System.out.println("📬 Сповіщення: " + msg.getContent());
                // Тут можна додати реальну відправку email
            } else {
                block();
            }
        }
    }

    protected void takeDown() {
        try { DFService.deregister(this); } catch (Exception ignored) {}
    }
}
