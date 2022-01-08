
import org.opensbpm.engine.api.instance.ObjectSchema
import org.opensbpm.engine.api.instance.AttributeSchema

println "Groovy Script: $task"

ObjectSchema objectSchema = task.getTaskDocument().getSchemas().first()

AttributeSchema attributeSchema = objectSchema.getAttributes().stream()
    .filter{attribute -> attribute.getName().equals("Field")}
    .findFirst()
    .orElse{null}

task.getTaskDocument().getAttribute(objectSchema,attributeSchema).setValue("Groovy Script")


