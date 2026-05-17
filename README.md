# wlm-multiagent-jade
# Мультиагентна система Wiki Loves Monuments (JADE)

Повна реалізація управління потоком задач фотоконкурсу за допомогою JADE.

## Як запустити
1. `mvn clean compile`
2. `mvn exec: java -Dexec.mainClass="ua.nure.lastovets.wlm.MainBoot"`
   Або через Jade.Boot:
   `java -cp target/... jade.Boot -gui coordinator:ua.nure.lastovets.wlm.CoordinatorAgent`

## Виконано
- Всі кроки JADE Tutorial for Beginners
- Contract Net Protocol
- DF registration
- ACL комунікація
