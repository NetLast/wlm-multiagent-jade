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

        // Запускаємо поведінку, яка чекає нові фотографії
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
            System.out.println("📋 Coordinator зареєстровано в DF (Yellow Pages)");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    /** Поведінка, яка отримує нові задачі (фотографії) */
    private class TaskReceiverBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                System.out.println("📸 Coordinator отримав нову фотографію: " + msg.getContent());
                addBehaviour(new TechValidationContractNet(myAgent, msg));
            } else {
                block();
            }
        }
    }

    /** Contract Net для технічної перевірки з динамічним пошуком агентів через DF */
    private class TechValidationContractNet extends ContractNetInitiator {

        public TechValidationContractNet(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        /** Підготовка CFP з пошуком всіх TechValidatorAgent через DF */
        protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {
            Vector<ACLMessage> cfps = new Vector<>();

            // Пошук агентів через DF
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("tech-validator");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);

                if (result.length == 0) {
                    System.out.println("⚠️ Не знайдено TechValidatorAgent!");
                    return cfps;
                }

                ACLMessage cfpTemplate = (ACLMessage) cfp.clone();
                cfpTemplate.setPerformative(ACLMessage.CFP);
                cfpTemplate.setConversationId("tech-check-" + System.currentTimeMillis());
                cfpTemplate.setReplyByDate(new Date(System.currentTimeMillis() + 15000));

                for (DFAgentDescription dfd : result) {
                    ACLMessage newCfp = (ACLMessage) cfpTemplate.clone();
                    newCfp.addReceiver(dfd.getName());
                    cfps.add(newCfp);
                    System.out.println("📤 Надіслано CFP до: " + dfd.getName().getLocalName());
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
            return cfps;
        }

        protected void handlePropose(ACLMessage propose, Vector acceptances) {
            ACLMessage accept = propose.createReply();
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            acceptances.add(accept);
            System.out.println("✅ Прийнято пропозицію від " + propose.getSender().getLocalName());
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println("✅ Технічна перевірка пройдена від " + inform.getSender().getLocalName() 
                    + ": " + inform.getContent());
            // Тут можна запустити наступний етап (тематична перевірка)
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("❌ Відмовлено: " + refuse.getSender().getLocalName());
        }
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception ignored) {}
        System.out.println("🛑 CoordinatorAgent " + getAID().getLocalName() + " завершено.");
    }
}
