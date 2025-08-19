A simple intelliJ plugin that automatically converts the var keyword to the explicit type during a variable declaration.

For example, a statement like this:
```java
var name = List.of(Map.of(1.2d, "Gaming"));
```

Upon hitting the semicolon will turn into
```java
List<@NotNull Map<@NotNull Float, String>> thing = List.of(Map.of(1.2f, "gamer"));
```

The plugin will also attempt to auto import anything that it can when it resolves the explicit type.

To install it download the jar file from the releases tab and go to Settings -> Plugins -> Plugin Settings -> Install from Disk -> VarAutoType1.0.jar
