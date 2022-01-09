
import org.opensbpm.engine.api.instance.ObjectSchema
import org.opensbpm.engine.api.instance.AttributeSchema

println "Groovy Script: $task"

ObjectSchema objectSchema = task.getSchemas().first()
task.getObjectBean(objectSchema).set("Field","Groovy Script")
