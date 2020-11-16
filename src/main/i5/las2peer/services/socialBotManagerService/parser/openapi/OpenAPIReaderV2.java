package i5.las2peer.services.socialBotManagerService.parser.openapi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.Json;

public class OpenAPIReaderV2 {

    public static Swagger readModel(String jsonUrl) {

	URI modelUri = null;
	try {
	    modelUri = new URI(jsonUrl);
	} catch (URISyntaxException e1) {
	    System.out.println("no valid uri");
	    e1.printStackTrace();
	}
	Swagger model = null;
	try {
	    model = processModel(modelUri.getPath());
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return model;

    }

    public static ServiceFunction readAction(Swagger model, String functionPath, String httpMethod) {

	Operation operation = getOperationByPath(model, functionPath, httpMethod);
	ServiceFunction action = parseAction(model, operation);
	action.setActionType(ActionType.OPENAPI);
	action.setHttpMethod(httpMethod);
	action.setFunctionPath(functionPath);
	action.setFunctionName(operation.getOperationId());
	action.setFunctionDescription(operation.getDescription());

	return action;

    }

    public static ServiceFunction readAction(Swagger model, String operationId) {

	Operation operation = getOperationByOperationId(model, operationId);
	ServiceFunction action = parseAction(model, operation);
	action.setActionType(ActionType.OPENAPI);
	action.setFunctionName(operation.getOperationId());
	action.setHttpMethod(getMethodByOperationId(model, operationId));
	action.setFunctionPath(getPathByOperationId(model, operationId));
	if (operation.getDescription() != null && !operation.getDescription().contentEquals(""))
	    action.setFunctionDescription(operation.getDescription());
	else if (operation.getSummary() != null && !operation.getSummary().contentEquals(""))
	    action.setFunctionDescription(operation.getSummary());

	return action;

    }

    private static Operation getOperationByPath(Swagger model, String path, String httpMethod) {

	// Path
	Path pathItem = model.getPaths().get(path);
	if (pathItem == null)
	    System.out.println("invalid path: " + path);

	// HTTP method
	Operation operation = null;
	switch (httpMethod) {
	case "get":
	    operation = pathItem.getGet();
	    break;
	case "post":
	    operation = pathItem.getPost();
	    break;
	case "put":
	    operation = pathItem.getPut();
	    break;
	case "delete":
	    operation = pathItem.getDelete();
	    break;
	default:
	    System.out.println("invalid http method: " + httpMethod);
	}

	if (operation == null)
	    System.out.println("no operation found");
	return operation;

    }

    private static Operation getOperationByOperationId(Swagger openAPI, String operationId) {

	System.out.println("get Operation by Id: " + operationId);

	for (Path pathItem : openAPI.getPaths().values()) {
	    if (pathItem.getGet() != null && pathItem.getGet().getOperationId().equals(operationId))
		return pathItem.getGet();
	    if (pathItem.getPost() != null && pathItem.getPost().getOperationId().equals(operationId))
		return pathItem.getPost();
	    if (pathItem.getPut() != null && pathItem.getPut().getOperationId().equals(operationId))
		return pathItem.getPut();
	    if (pathItem.getDelete() != null && pathItem.getDelete().getOperationId().equals(operationId))
		return pathItem.getDelete();
	}

	System.out.println("Operation not found");
	return null;
    }

    private static String getMethodByOperationId(Swagger openAPI, String operationId) {

	System.out.println("find http method for operationId: " + operationId);
	for (Path pathItem : openAPI.getPaths().values()) {
	    if (pathItem.getGet() != null && pathItem.getGet().getOperationId().equals(operationId))
		return "get";
	    if (pathItem.getPost() != null && pathItem.getPost().getOperationId().equals(operationId))
		return "post";
	    if (pathItem.getPut() != null && pathItem.getPut().getOperationId().equals(operationId))
		return "put";
	    if (pathItem.getDelete() != null && pathItem.getDelete().getOperationId().equals(operationId))
		return "delete";
	}

	return null;
    }

    private static String getPathByOperationId(Swagger openAPI, String operationId) {

	for (Entry<String, Path> entry : openAPI.getPaths().entrySet()) {
	    Path pathItem = entry.getValue();
	    if (pathItem.getGet() != null && pathItem.getGet().getOperationId().equals(operationId))
		return entry.getKey();
	    if (pathItem.getPost() != null && pathItem.getPost().getOperationId().equals(operationId))
		return entry.getKey();
	    if (pathItem.getPut() != null && pathItem.getPut().getOperationId().equals(operationId))
		return entry.getKey();
	    if (pathItem.getDelete() != null && pathItem.getDelete().getOperationId().equals(operationId))
		return entry.getKey();
	}

	return null;
    }

    private static ServiceFunction parseAction(Swagger swagger, Operation operation) {
	ServiceFunction action = new ServiceFunction();

	assert swagger != null : "no model specified";
	assert operation != null : "no operation specified";

	// Operation ID
	if (operation.getOperationId() != null) {
	    String operationId = operation.getOperationId();
	    action.setFunctionName(operationId);
	} else {
	    System.out.println("no operation id");
	}

	// Parameters
	List<Parameter> parameters = operation.getParameters();
	if (operation.getParameters() != null) {
	    for (Parameter parameter : parameters) {

		ServiceFunctionAttribute attr = new ServiceFunctionAttribute();

		attr.setRequired(parameter.getRequired());
		if (parameter.getName() != null)
		    attr.setName(parameter.getName());
		if (parameter.getDescription() != null)
		    attr.setDescription(parameter.getDescription());
		if (parameter.getIn() != null)
		    attr.setContentType(parameter.getIn());

		// Body Parameter
		if (parameter.getIn().contentEquals("body")) {
		    String ref = ((BodyParameter) parameter).getSchema().getReference()
			    .substring("#/definitions/".length());

		    String name = ref;
		    attr.setName(name);
		    attr = addChildrenAttributes(swagger, ref, attr);
		    attr.setParameterType(ParameterType.BODY);

		}
		action.addAttribute(attr);
	    }

	}

	// consumes
	if (operation.getConsumes() != null) {
	    if (operation.getConsumes().contains("application/json"))
		action.setConsumes("application/json");
	}

	// produces
	if (operation.getProduces() != null) {
	    if (operation.getProduces().contains("application/json"))
		action.setProduces("application/json");
	}

	return action;

    }

    private static ServiceFunctionAttribute addChildrenAttributes(Swagger openAPI, String ref,
	    ServiceFunctionAttribute bodyAttribute) {
	return addChildrenAttributes(openAPI, ref, bodyAttribute, null, 0);
    }

    private static ServiceFunctionAttribute addChildrenAttributes(Swagger swagger, String ref,
	    ServiceFunctionAttribute parentAttr, String dis, int rec) {

	Model model = swagger.getDefinitions().get(ref);
	assert model != null : "model not found by ref: " + ref;

	// Discriminator
	if (dis == null)
	    dis = getDiscriminator(swagger, model);

	if (model instanceof ComposedModel) {
	    ComposedModel cm = (ComposedModel) model;
	    model = cm.getChild();
	}

	Json.prettyPrint(model.getClass());
	// Add properties of model to attribute
	if (model.getProperties() == null) {
	    System.out.println("schema " + model.getTitle() + " has no properties. Skip");
	} else {

	    Map<String, Property> properties = model.getProperties();
	    for (Map.Entry<String, Property> pair : properties.entrySet()) {
		System.out.println("property: " + pair.getKey());

		String name = pair.getKey();
		Property property = pair.getValue();

		ServiceFunctionAttribute childAttr = new ServiceFunctionAttribute();
		childAttr.setName(name);

		// discriminator
		System.out.println("name: " + name + "  dis: " + dis);
		if (dis != null && dis.contentEquals(name)) {
		    childAttr.setParameterType(ParameterType.DISCRIMINATOR);
		    childAttr = processAttribute(property, childAttr);
		    childAttr.setRequired(true);

		    List<String> modelRefs = getSubModels(swagger, ref);
		    parentAttr.addChildAttribute(childAttr);
		    childAttr.setParent(parentAttr);
		    for (String sm : modelRefs) {
			parentAttr = addChildrenAttributes(swagger, sm, parentAttr, sm, rec);
		    }

		} else {
		    childAttr.setParameterType(ParameterType.CHILD);
		    if (dis != null)
			childAttr.setDiscriminator(dis);

		    // nested schemas
		    if (property instanceof RefProperty) {
			childAttr.setContentType("object");
		    } else {
			childAttr = processAttribute(property, childAttr);
		    }

		    if (childAttr.getContentType().contentEquals("object")) {
			if (rec > 8) {
			    System.out.println("to much nesting");
			} else {

			    if (property instanceof RefProperty) {
				ref = ((RefProperty) property).get$ref().substring("#/definitions/".length());
				childAttr = addChildrenAttributes(swagger, ref, childAttr, dis, rec + 1);
			    }

			    if (property instanceof ArrayProperty) {
				ArrayProperty arrayProperty = (ArrayProperty) property;
				if (arrayProperty.getItems() != null) {
				    ref = ((RefProperty) arrayProperty.getItems()).get$ref()
					    .substring("#/definitions/".length());
				    childAttr = addChildrenAttributes(swagger, ref, childAttr, dis, rec + 1);
				}
			    }

			}
		    }
		    if (childAttr != null) {
			parentAttr.addChildAttribute(childAttr);
			childAttr.setParent(parentAttr);
		    }

		}

	    }
	}

	return parentAttr;

    }

    private static ServiceFunctionAttribute processAttribute(Property property, ServiceFunctionAttribute attr) {

	// parameter description (optional)
	if (property.getDescription() != null)
	    attr.setDescription(property.getDescription());

	// parameter example value (optional)
	if (property.getExample() != null)
	    attr.setExample(property.getExample().toString());

	// required
	attr.setRequired(property.getRequired());

	// parameter type
	switch (property.getType()) {
	case "integer":
	    attr.setContentType("integer");
	    break;
	case "string":
	    StringProperty stringProperty = (StringProperty) property;
	    attr.setContentType("string");

	    if (property.getFormat() != null && property.getFormat().contentEquals("url"))
		attr.setContentType("url");

	    if (stringProperty.getEnum() != null && stringProperty.getEnum().size() > 0) {
		attr.setContentType("enum");
		attr.setEnumList(stringProperty.getEnum());
	    }
	    break;
	case "boolean":
	    attr.setContentType("boolean");
	    break;
	case "array":
	    attr.setArray(true);
	    ArrayProperty arrayProperty = (ArrayProperty) property;
	    String type = arrayProperty.getItems().getType();
	    attr.setContentType(type);
	    if (arrayProperty.getItems() instanceof RefProperty) {
		System.out.println("array with ref objects");
		attr.setContentType("object");
	    }

	    break;
	default:
	    System.out.println("unknown parameter content type");
	}

	return attr;
    }

    private static Swagger processModel(String modelUri) throws Exception {

	SwaggerDeserializationResult swaggerParseResult = new SwaggerParser().readWithInfo(modelUri, null, true);
	Swagger swagger = swaggerParseResult.getSwagger();

	System.out.printf("== Model %s\n", modelUri);
	System.out.printf("------\n\n");
	return swagger;
    }

    public static String getDiscriminator(Swagger swagger, Model model) {
	if (model instanceof ModelImpl) {
	    ModelImpl mi = (ModelImpl) model;
	    if (mi.getDiscriminator() != null && mi.getDiscriminator() != "")
		return mi.getDiscriminator();
	}
	return null;
    }

    public static List<String> getSubModels(Swagger swagger, String ref) {
	List<String> result = new ArrayList<String>();

	for (Map.Entry<String, Model> entry : swagger.getDefinitions().entrySet()) {
	    if (entry.getValue() instanceof ComposedModel) {
		ComposedModel cm = (ComposedModel) entry.getValue();
		for (Model allOfModel : cm.getAllOf()) {
		    if (allOfModel instanceof RefModel) {
			String ref2 = ((RefModel) allOfModel).get$ref();
			String ref2sub = ref2.substring("#/definitions/".length());
			if (ref.contentEquals(ref2sub) || ref.contentEquals(ref2))
			    result.add(entry.getKey());

		    }
		}

	    }

	}
	return result;
    }

}
