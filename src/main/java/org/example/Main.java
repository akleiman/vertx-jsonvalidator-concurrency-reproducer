package org.example;

import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    private static final String SCHEMA_START = """
            {"": "https://json-schema.org/draft/2019-09/schema", "type": "object", "properties": {"PROP_NAME": {"type": "string"}}}""";

    private static final String JSON = """
            {"PROP_NAME": "x"}""";

    private final SchemaRepository schemaRepository;
    private final List<String> props;
    private final ThreadFactory threadFactory;
    private final int num;
    private final ConcurrentHashMap<String, Validator> validators;


    public Main(SchemaRepository schemaRepository, List<String> props, ThreadFactory threadFactory, int num) {
        this.schemaRepository = schemaRepository;
        this.props = props;
        this.threadFactory = threadFactory;
        this.num = num;
        validators = new ConcurrentHashMap<>();
    }

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Starting");
        SchemaRepository schemaRepository = SchemaRepository.create(
                new JsonSchemaOptions().setDraft(Draft.DRAFT202012)
                        .setBaseUri("http://merchant-profile-ingestor.ifood.internal")
        );
        int num = 20;
        List<String> props = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            props.add("propName_" + i);
        }
        ThreadFactory factory = Thread.ofVirtual().name("Main").factory();
        Main main = new Main(schemaRepository, props, factory, num);
        factory.newThread(main::scheduleThreads).start();
        Thread.sleep(10000);

    }

    private void scheduleThreads() {
        int i = 0;
        while (true) {
            int myI = i;
            threadFactory.newThread(() -> {
                validate(props.get(myI % num));
            }).start();
            i++;
            try {
                if (i % num == 0) {
                    System.out.println("i= " + i);
                    Thread.sleep(100);
                } else if (i >= num*5) {
                    return;
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void validate(String propname) {
        Validator validator = validators.computeIfAbsent(propname, (key) -> {
            String schema = SCHEMA_START.replace("PROP_NAME", key);
            JsonSchema jsonSchema = JsonSchema.of(new JsonObject(schema));
            schemaRepository.dereference(key, jsonSchema);
            return schemaRepository.validator(key);
        });
        validator.validate(JSON.replace("PROP_NAME", propname));
    }
}