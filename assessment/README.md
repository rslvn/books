# assessment

## build
```
mvn clean verify
mvn -Pcargo.run -Drepo.path=storage
```

## File structure
```
resulav@resula-pc:/ws/books/assessment/src$ tree
.
├── main
│   ├── java
│   │   └── org
│   │       └── example
│   │           └── assessment
│   │               ├── BookModule.java
│   │               ├── BookStoreModule.java
│   │               ├── common
│   │               │   ├── BookField.java
│   │               │   ├── Constants.java
│   │               │   └── ResultCode.java
│   │               ├── exception
│   │               │   └── BookException.java
│   │               ├── HelloModule.java
│   │               ├── model
│   │               │   ├── Book.java
│   │               │   └── BookResponse.java
│   │               ├── resource
│   │               │   ├── BookResources.java
│   │               │   └── BookStoreResources.java
│   │               ├── service
│   │               │   └── BookService.java
│   │               ├── store
│   │               │   ├── BookCache.java
│   │               │   └── BookObservator.java
│   │               └── util
│   │                   ├── BookUtil.java
│   │                   ├── Preconditions.java
│   │                   └── RepositoryUtil.java
│   └── resources
│       ├── hcm-config
│       │   └── main.yaml
│       └── hcm-module.yaml
└── test
    ├── java
    │   └── org
    │       └── example
    │           └── assessment
    │               ├── exception
    │               │   └── BookExceptionTest.java
    │               ├── JaxrsTest.java
    │               ├── model
    │               │   └── ModelTest.java
    │               └── RepositoryTest.java
    └── resources
        ├── book_assessment.postman_collection.json
        └── log4j2.xml

22 directories, 25 files
```
## test via `postman`

import postman file  [book_assessment.postman_collection.json](./src/test/resources/book_assessment.postman_collection.json) from ./src/test/resources/book_assessment.postman_collection.json

## test via `curl`

### add books
```
curl -H "Content-Type: application/json" -X POST -u admin:admin http://localhost:8080/cms/ws/books/ -d '
[
{
    "name": "Mice And Dogs",
    "author": "Joanna Shaffer",
    "isbn": "95-8929-676-7",
    "introduction": [
      "Lorem ipsum dolor sit amet, ea eum mandamus persequeris, ei quem fugit suscipiantur nec, dolorum graecis eam ea. Id tollit erroribus disputationi per, qui te quaeque epicuri conceptam. Vim te bonorum deseruisse. Vel id nibh ferri veritus, audiam timeam pro id. In alterum rationibus est, id inermis appellantur vel.",
      "At pri aliquip hendrerit complectitur, enim omnium molestiae cu quo. Nostrum mentitum nominavi vim ea, adhuc vocibus percipitur cu pri. At eum malis maiorum, sit ne aliquam feugiat constituam. Nam te natum habeo conclusionemque, pro sint adhuc id. Quo vocent ceteros moderatius an, per ut erat possim graecis."
    ],
    "paragraphs": [
      "Lorem ipsum dolor sit amet, delenit epicurei invidunt eu sea. Vitae luptatum cu eam, apeirian vituperatoribus vix cu. Eos ut fierent posidonium, nec dicam homero et. Sed nulla dolore ut.",
      "Ad sale putant dignissim usu, his legimus scriptorem id. Dolorem legendos nam ad, ne stet nostrud incorrupte his. Dolorum repudiandae vix an, ea partem impetus persequeris nec. An has mucius iriure, ex duo mundi conclusionemque. Eum suas nulla noluisse no, vel at suscipit corrumpit persecuti, sit iudico appareat vituperata no.",
      "Ea sed viris vocent timeam, nec qualisque efficiendi ad, ad aliquid habemus dissentias vix. Duo at diam eros voluptaria. An mei elit ipsum labores, dicant nullam mel at. At cum recteque efficiendi. Voluptua accusata ex eos, pri scripta dolorem percipit ea."
    ]
  }
]'
```
### get books from repository
```
curl -H "Content-Type: application/json" -X GET -u admin:admin http://localhost:8080/cms/ws/books/
```
### get books from store
```
curl -H "Content-Type: application/json" -X GET -u admin:admin http://localhost:8080/cms/ws/bookstore/
```
### search books in repository
```
curl -H "Content-Type: application/json" -X GET -u admin:admin http://localhost:8080/cms/ws/books/search/Shaffer
```
### update a book in repository
```
curl -H "Content-Type: application/json" -X POST -u admin:admin http://localhost:8080/cms/ws/books/update -d '
{
    "bookId":"8394e063-807e-4bf5-8c15-b21804d803e0",
    "name": "Mice And Dogs",
    "author": "Joanna Shaffer",
    "isbn": "95-8929-676-7",
    "introduction": [
      "UPDATED Lorem ipsum dolor sit amet, ea eum mandamus persequeris, ei quem fugit suscipiantur nec, dolorum graecis eam ea. Id tollit erroribus disputationi per, qui te quaeque epicuri conceptam. Vim te bonorum deseruisse. Vel id nibh ferri veritus, audiam timeam pro id. In alterum rationibus est, id inermis appellantur vel.",
      "At pri aliquip hendrerit complectitur, enim omnium molestiae cu quo. Nostrum mentitum nominavi vim ea, adhuc vocibus percipitur cu pri. At eum malis maiorum, sit ne aliquam feugiat constituam. Nam te natum habeo conclusionemque, pro sint adhuc id. Quo vocent ceteros moderatius an, per ut erat possim graecis."
    ],
    "paragraphs": [
      "UPDATED Lorem ipsum dolor sit amet, delenit epicurei invidunt eu sea. Vitae luptatum cu eam, apeirian vituperatoribus vix cu. Eos ut fierent posidonium, nec dicam homero et. Sed nulla dolore ut.",
      "Ad sale putant dignissim usu, his legimus scriptorem id. Dolorem legendos nam ad, ne stet nostrud incorrupte his. Dolorum repudiandae vix an, ea partem impetus persequeris nec. An has mucius iriure, ex duo mundi conclusionemque. Eum suas nulla noluisse no, vel at suscipit corrumpit persecuti, sit iudico appareat vituperata no.",
      "Ea sed viris vocent timeam, nec qualisque efficiendi ad, ad aliquid habemus dissentias vix. Duo at diam eros voluptaria. An mei elit ipsum labores, dicant nullam mel at. At cum recteque efficiendi. Voluptua accusata ex eos, pri scripta dolorem percipit ea."
    ]
  }'
```
### delete a book from repository
```
curl -H "Content-Type: application/json" -X DELETE -u admin:admin http://localhost:8080/cms/ws/books/delete/8394e063-807e-4bf5-8c15-b21804d803e0
```

## result codes

```
	SUCCESS(0),
	FAILED(1),
	NOT_FOUND(2),
	ALREADY_EXIST(3),
	VALIDATION_FAILED(4),
```

## Responses

### Get/Search APIs

Search and get APIs return a book array as json

#### sample success response
```
[
  {
    "name": "Mice And Dogs",
    "author": "Joanna Shaffer",
    "isbn": "95-8929-676-7",
    "introduction": [
      "Lorem ipsum dolor sit amet, ea eum mandamus persequeris, ei quem fugit suscipiantur nec, dolorum graecis eam ea. Id tollit erroribus disputationi per, qui te quaeque epicuri conceptam. Vim te bonorum deseruisse. Vel id nibh ferri veritus, audiam timeam pro id. In alterum rationibus est, id inermis appellantur vel.",
      "At pri aliquip hendrerit complectitur, enim omnium molestiae cu quo. Nostrum mentitum nominavi vim ea, adhuc vocibus percipitur cu pri. At eum malis maiorum, sit ne aliquam feugiat constituam. Nam te natum habeo conclusionemque, pro sint adhuc id. Quo vocent ceteros moderatius an, per ut erat possim graecis."
    ],
    "paragraphs": [
      "Lorem ipsum dolor sit amet, delenit epicurei invidunt eu sea. Vitae luptatum cu eam, apeirian vituperatoribus vix cu. Eos ut fierent posidonium, nec dicam homero et. Sed nulla dolore ut.",
      "Ad sale putant dignissim usu, his legimus scriptorem id. Dolorem legendos nam ad, ne stet nostrud incorrupte his. Dolorum repudiandae vix an, ea partem impetus persequeris nec. An has mucius iriure, ex duo mundi conclusionemque. Eum suas nulla noluisse no, vel at suscipit corrumpit persecuti, sit iudico appareat vituperata no.",
      "Ea sed viris vocent timeam, nec qualisque efficiendi ad, ad aliquid habemus dissentias vix. Duo at diam eros voluptaria. An mei elit ipsum labores, dicant nullam mel at. At cum recteque efficiendi. Voluptua accusata ex eos, pri scripta dolorem percipit ea."
    ]
  }
]
```

#### sample fail or empty response
```
[]
```

### Add/Update/Delete APIs
#### sample success response
```
{
  "resultCode": 0,
  "resultName": "SUCCESS",
  "resultMessage": null
}
```

#### sample fail response
```
{
  "resultCode": 4,
  "resultName": "VALIDATION_FAILED",
  "resultMessage": "books can not be empty"
}
```