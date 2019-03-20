# README

# Cookies
This application uses the following cookies

- `navigationTabs`.  Controls whether the navigation panel contains separate tabs for entities and enumerations
- `classDisplayName`. Specifies whether the business or technical name is displayed for classes
- `classListSrc`.  The `src` attribute for the navigation panel on the left-hand side of the window.  This value is derived from `navigationTabs` and `classDisplayName`.

# Left Navigation
The following cookies affect the look-and-feel of the Left Navigation panel.

1. `navigationTabs`:  One of the following values:
   - `AllInOne`.  Display entities and enumerations together in a single panel.
   - `SeparateTabs`.  Display entities and enumerations in separate tabs
2. `classDisplayName`:  One of the following values:
   - `business`.  Display the human-friendly business name given by schema:name, rdfs:label, or falling back to the Technical Name.
   - `technical`.  Display the technical name, also known as the local name of the OWL class.
   
These two options result in the following options for the navigation panel source:

| classDisplayName | navigationTabs   | classListSrc                                                |
| ---------------- | ---------------- | ----------------------------------------------------------- |
| business         | AllInOne         | index-business-classes.html                                 |
| business         | SeparateTabs     | index-business-entities.html<br>index-business-enums.html   |
| technical        | AllInOne         | index-technical-classes.html                                |
| technical        | SeparateTabs     | index-technical-entities.html<br>index-technical-enums.html |


