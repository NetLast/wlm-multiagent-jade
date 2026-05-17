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

            System.out.println("🚀 Мультиагентна система WLM запущена!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
