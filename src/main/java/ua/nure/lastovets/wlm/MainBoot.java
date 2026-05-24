package ua.nure.lastovets.wlm;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MainBoot {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            p.setParameter(Profile.MAIN_HOST, "localhost");
            p.setParameter(Profile.GUI, "true");

            AgentContainer mainContainer = rt.createMainContainer(p);

            // Запуск Coordinator
            AgentController coordinator = mainContainer.createNewAgent(
                "coordinator",
                "ua.nure.lastovets.wlm.CoordinatorAgent",
                null);
            coordinator.start();

            // Запуск кількох валідаторів
            for (int i = 1; i <= 3; i++) {
                mainContainer.createNewAgent(
                    "tech-validator-" + i,
                    "ua.nure.lastovets.wlm.TechValidatorAgent",
                    null).start();
            }

            for (int i = 1; i <= 2; i++) {
                mainContainer.createNewAgent(
                    "topic-validator-" + i,
                    "ua.nure.lastovets.wlm.TopicValidatorAgent",
                    null).start();
            }

            // Запуск JuryDispatcherAgent
            mainContainer.createNewAgent(
                "jury-dispatcher",
                "ua.nure.lastovets.wlm.JuryDispatcherAgent",
                null).start();

            // Запуск JuryMemberAgent
            for (String name : new String[]{"jury-member-1", "jury-member-2"}) {
                mainContainer.createNewAgent(
                    name,
                    "ua.nure.lastovets.wlm.JuryMemberAgent",
                    null).start();
            }

            // Запуск NotificationAgent
            mainContainer.createNewAgent(
                "notifier",
                "ua.nure.lastovets.wlm.NotificationAgent",
                null).start();

            System.out.println("🚀 Мультиагентна система WLM запущена!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
