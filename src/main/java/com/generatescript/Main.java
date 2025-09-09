package com.generatescript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static String scriptExample = """
            import br.com.telefonica.core.service.VivoBusinessProcessService
            import de.hybris.platform.orderprocessing.model.OrderProcessModel
            import de.hybris.platform.servicelayer.config.ConfigurationService
            import de.hybris.platform.servicelayer.search.FlexibleSearchQuery
            import de.hybris.platform.servicelayer.search.FlexibleSearchService
            VivoBusinessProcessService vivoBusinessProcessService = spring.getBean("vivoBusinessProcessService");
            
            List<OrderProcessModel> getAllTaskLogNokBySavedQuery() {
            println "getAllTaskLogNokBySavedQuery"
            FlexibleSearchService flexibleSearchService = spring.getBean("flexibleSearchService");
            println "getAllTaskLogNokBySavedQuery savedQueryModel"
            FlexibleSearchQuery searchQuery = new FlexibleSearchQuery("SELECT DISTINCT {pr.pk} FROM {orderprocess AS pr JOIN ORDER AS o ON {pr.order} = {o.pk} JOIN abstractorderentry AS e ON {o.pk} = {e.order} JOIN product AS p ON {e.product} = {p.pk}} WHERE  {pr.code} LIKE '{processName}%' AND {o.code} IN ({items})");
            searchQuery.setDisableCaching(true);
            List<OrderProcessModel> result = flexibleSearchService.search(searchQuery).getResult();
            return result;
            }
            
            List<OrderProcessModel> processDataList = getAllTaskLogNokBySavedQuery();
            println "processes found"
            for (OrderProcessModel item : processDataList) {
            println "Execute Process = "+item.getCode();
            vivoBusinessProcessService.restartProcess(item, "createGedocContract");
            };
            """;

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("\u001b[32m======================== Dúvidas chamar joao.soliveira@telefonica.com ========================\u001b[0m");
        if (!new File("./reprocessar.csv").exists()) {
            System.out.println("Arquivo 'reprocessar.csv' nao encontrado.");
            return;
        }
        ArrayList<Item> items = new ArrayList<>();
        Scanner scanner = new Scanner(new File("./reprocessar.csv"));

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().replaceAll("\uFEFF", "");
            String[] splitted = line.split(";");
            Item item = new Item(splitted[0], splitted[1]);
            items.add(item);
        }

        items.sort(Comparator.comparing(Item::getProcessDefinitionName));

        HashSet<Object> processesDefinitions = new HashSet<>();
        items.forEach(i -> processesDefinitions.add(i.getProcessDefinitionName()));

        AtomicInteger scriptsCounter = new AtomicInteger(1);
        AtomicReference<StringBuilder> itemsStr = new AtomicReference<>(new StringBuilder());

        processesDefinitions.forEach(processDefinition -> {
            List<Item> currentItems = items.stream().filter(item -> item.getProcessDefinitionName().equals(processDefinition)).toList();

            Integer loops = (int) Math.ceil((double) currentItems.size() / 1000);
            Integer currentLoop = 1;
            for (int i = 0; i < loops * 1000; i += 1000) {
                currentItems.subList(i, Math.min(currentItems.size(), currentLoop * 1000)).forEach(item -> {
                    if (itemsStr.get().isEmpty()) {
                        itemsStr.get().append("'").append(item.getProductCode()).append("'");
                    } else {
                        itemsStr.get().append(",'").append(item.getProductCode()).append("'");
                    }
                });

                try (PrintWriter writer = new PrintWriter("script" + scriptsCounter + "-" + processDefinition + ".groovy")) {
                    String script = scriptExample
                            .replaceAll("\\{processName}", processDefinition.toString().replaceAll("[0-9]", ""))
                            .replaceAll("\\{items}", itemsStr.toString());
                    writer.print(script);
                } catch (Exception e) {
                }
                itemsStr.set(new StringBuilder());
                scriptsCounter.getAndIncrement();
                currentLoop++;
            }

        });
    }
}