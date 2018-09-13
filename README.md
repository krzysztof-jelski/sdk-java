# Ambrosus Java Development Kit (Android API 21 compatible)
## Features (100% code coverage, 13.09.2018)
### Creation
* Create assets
* Create events
* Create accounts
* Create tokens

### Fetching
* Retrieve assets by ID
* Retrieve events by asset ID 
* Retrieve accounts by address 
* Find assets with parameters 
* Find events with parameters 
* Find accounts with parameters 

### Crypto
* Compute signatures
* Compute ethereum hashes
* Verify asset and event signatures
* Verify event data hashes

### Advanced
* Automatic serialization/deserialization of HTTP server responses into Java classes

## Getting started
### As a Gradle module
Add the following to your `settings.gradle` file:
```
include ':sdk'
project(':sdk').projectDir = new File(<path-to-your-sdk>)
```

And then to your module `build.gradle` add the following dependency:
```
implementation project(path: ':sdk')
```

### Using a JAR 
Simply add the .jar release of the SDK as a JAR dependency to your project.

## API Documentation
The documentation is located in the [docs folder](../master/doc).

## Examples
See the [example folder](../master/src/main/java/com.ambrosus.examples).

## Documentation
For a basic usage of the SDK, see directly the com.ambrosus.examples.
### Automatic Event Data Deserialization
Thanks to Google's Gson library, it is possible to automatically serialize and deserialize the HTTP responses sent by the server into proper Java classes. 

If the object you want to transform back and forth between JSON and Java representations has a simple structure, you just need to tell the SDK to associate an Ambrosus event data type name (such as `ambrosus.asset.info`) with the corresponding java class. Whenever an event data object with the given type is encountered, Gson will try to automatically deserialize it into the Java class. If you are not satisfied with the default behavior or if the structure of your object is too complex, you can register your own adapter. To do this, two steps are required:
First, you have to create an adapter class whose job is to translate your Java class to and from a Json object. It should implement Gson interfaces `JsonSerializer<T>` and `JsonDeserializer<T>` where T is the type of your custom event data class you want to serialize/deserialize automatically.
Second, register your adapter in the SDK by calling the `registerEventDataType(String, Class, Object)` method. This method requires 3 components.
1. A string describing the event type as stored on AMBNet. For example, ambrosus.asset.info or ambrosus.event.identifiers are two such strings. 
2. The class type of your custom event data class.
3. An instance of your custom event data class adapter.
This allows the SDK to provide the Gson serializer/deserializer with the correct adapter when it encounters an event with the given type.
The `com.ambrosus.commons` package already provides classes and adapters for event data types commonly found on AMBNet. They can be used as an example to write your own adapter. 

