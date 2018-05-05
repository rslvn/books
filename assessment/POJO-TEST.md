# Dependency: pojo-tester

```
<repositories>
<repository>
  <id>jcenter</id>
  <url>http://jcenter.bintray.com/</url>
</repository>
</repositories>

<dependency>
  <groupId>pl.pojo</groupId>
  <artifactId>pojo-tester</artifactId>
  <version>0.7.5</version>
  <type>pom</type>
</dependency>

```


More Information:   [website](http://www.pojo.pl/),
                    [github](https://github.com/sta-szek/pojo-tester)


# Usage:

A pojo class can contain constructors, setters, getters, hashCode, equals and toString methods.

Check your pojo class add method types to testing method.

Which method type is used for:
- **constructors**  : use ``` Method.CONSTRUCTOR ```
- **setters**       : use ``` Method.SETTER ```
- **getters**       : use ``` Method.GETTER ```
- **hashCode**      : use ``` Method.HASH_CODE ```
- **equals**        : use ``` Method.EQUALS ```  
- **toString**      : use ``` Method.TO_STRING ```


>   If your class have all method types, just add  **Method.values()** to testing method.

##### Sample: 

To test BookTest.java pojo class. Add a test class and add following codes in the class.

>`Book.java doesn't have setters. Because of that we don't add `**Method.SETTER**` to testing method.`

```
    @Test
    public void testBook() {
        Assertions.assertPojoMethodsFor(Book.class)
                .testing(Method.CONSTRUCTOR, Method.GETTER, Method.EQUALS,
                         Method.HASH_CODE, Method.TO_STRING)
                .areWellImplemented();
    }

```


Full example:   [BookTest.java](./src/test/java/org.example/myhippoproject/model/BookTest.java)