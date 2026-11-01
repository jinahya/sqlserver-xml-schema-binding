# sqlserver-xml-schema-binding

[Jakarta XML Binding](https://jakarta.ee/specifications/xml-binding/) classes for the
[Microsoft SQL Server XML Schemas](https://schemas.microsoft.com/sqlserver/).

## Modules

One module per schema `targetNamespace`. Names are derived from the namespace, so a new schema never needs a name to be
chosen:

| what                              | value                                                                   |
|-----------------------------------|-------------------------------------------------------------------------|
| token                             | `b` + first 4 hex chars of SHA-256 of the exact `targetNamespace` + `e` |
| module directory / artifactId     | `sqlserver-xml-schema-binding-{token}`                                  |
| package / `Automatic-Module-Name` | `io.github.jinahya.sqlserver.xml.schema.binding.{token}`                |

```sh
printf '%s' "http://schemas.microsoft.com/sqlserver/2004/07/showplan" | shasum -a 256 | cut -c1-4   # a1b3 -> ba1b3e
```

| `targetNamespace` (`http://schemas.microsoft.com/sqlserver/`…) | description                                                     | artifactId                            |
|----------------------------------------------------------------|-----------------------------------------------------------------|---------------------------------------|
| `2004/07/adventure-works/ContactInfo`                          | AdventureWorks Customer Contact Information                     | `sqlserver-xml-schema-binding-b7fdbe` |
| `2004/07/adventure-works/ContactRecord`                        | AdventureWorks Customer Contact Record                          | `sqlserver-xml-schema-binding-b80e2e` |
| `2004/07/adventure-works/ContactTypes`                         | AdventureWorks Customer Contact Type                            | `sqlserver-xml-schema-binding-b2e7ae` |
| `2004/07/adventure-works/IndividualSurvey`                     | AdventureWorks Consumer Demographics Survey                     | `sqlserver-xml-schema-binding-b9c1be` |
| `2004/07/adventure-works/ProductModelDescription`              | AdventureWorks Product Catalog                                  | `sqlserver-xml-schema-binding-b0fd0e` |
| `2004/07/adventure-works/ProductModelWarrAndMain`              | AdventureWorks Product Warranty and Maintenance Information     | `sqlserver-xml-schema-binding-b4347e` |
| `2004/07/adventure-works/Resume`                               | AdventureWorks Standard Resume                                  | `sqlserver-xml-schema-binding-b3da3e` |
| `2004/07/adventure-works/StoreSurvey`                          | AdventureWorks Retail Stores Demographics Survey                | `sqlserver-xml-schema-binding-b95f5e` |
| `2004/07/dta`                                                  | Database Engine Tuning Advisor                                  | `sqlserver-xml-schema-binding-b89eee` |
| `2004/07/queryprocessor`                                       | Memory Grant                                                    | `sqlserver-xml-schema-binding-b61cee` |
| `2004/07/showplan`                                             | Showplan                                                        | `sqlserver-xml-schema-binding-ba1b3e` |
| `2004/bulkload/format`                                         | Bulk Load Format                                                | `sqlserver-xml-schema-binding-bc42fe` |
| `2004/sqltypes`                                                | SQL Server to XSD Type Mapping                                  | `sqlserver-xml-schema-binding-bdd36e` |
| `2004/SOAP/Options`                                            | SQL SOAP Options                                                | `sqlserver-xml-schema-binding-bf91ee` |
| `2004/SOAP/types/SqlMessage`                                   | SQL SOAP Types SqlMessage                                       | `sqlserver-xml-schema-binding-b8bc1e` |
| `2004/SOAP/types/SqlParameter`                                 | SQL SOAP Types SqlParameter                                     | `sqlserver-xml-schema-binding-b2898e` |
| `2004/SOAP/types/SqlRowCount`                                  | SQL SOAP Types SqlRowCount                                      | `sqlserver-xml-schema-binding-b3f54e` |
| `2008/07/extendedeventsconfig`                                 | SQL Server Extended Events Configuration                        | `sqlserver-xml-schema-binding-b1232e` |
| `http://www.microsoft.com/SqlServer/Dts/DataTypeMapping.xsd`   | SQL Server Import and Export Wizard Data Type Mapping           | `sqlserver-xml-schema-binding-b1464e` |
| `http://www.microsoft.com/SqlServer/Dts/DtwTypeConversion.xsd` | SQL Server Import and Export Wizard Data Type Conversion        | `sqlserver-xml-schema-binding-b8e89e` |
| `2008/10/servicebroker/externalactivator`                      | SQL Server Service Broker External Activator Configuration File | `sqlserver-xml-schema-binding-b2707e` |
| `2008/DataDebugger/`                                           | Integration Services Data Profile                               | `sqlserver-xml-schema-binding-bd290e` |
| `2011/geography`                                               | SQL Server GEOGRAPHY Type Extension                             | `sqlserver-xml-schema-binding-b2a48e` |
| `dac/DeployReport/2012/02`                                     | SQL Server DacFx Deploy Report                                  | `sqlserver-xml-schema-binding-b13d6e` |
| `dac/DriftReport/2012/02`                                      | SQL Server DacFx Drift Report                                   | `sqlserver-xml-schema-binding-baccbe` |
| `dac/Serialization/2012/02`                                    | SQL Server DacFx Serialization                                  | `sqlserver-xml-schema-binding-b669ce` |
| `http://www.opengis.net/gml`                                   | SQL Server's profile of the GML Schema                          | `sqlserver-xml-schema-binding-bfdede` |
| `reporting/2005/01/reportdefinition`                           | Report Definition Language (RDL) 2005                           | `sqlserver-xml-schema-binding-be1d1e` |
| `reporting/2008/01/reportdefinition`                           | Report Definition Language (RDL) 2008                           | `sqlserver-xml-schema-binding-b2a2ce` |
| `reporting/2010/01/reportdefinition`                           | Report Definition Language (RDL) 2010                           | `sqlserver-xml-schema-binding-b9cace` |

Generated classes depend only on `jakarta.xml.bind:jakarta.xml.bind-api`; bring your own runtime (e.g.
`org.glassfish.jaxb:jaxb-runtime`).

## Schemas

The XML schemas are © Microsoft Corporation and are not included in this repository or its jars. Download them before
building:

```sh
mvn -N antrun:run@download-schemas
mvn verify
```
