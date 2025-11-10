| METHOD | Endpoint                                           | Tomcat 9 | Spring Boot |
|--------|----------------------------------------------------|----------|-------------|
| PUT    | /api/v1/functions/{id}/signature                   | 634ms    | 290ms       |
| DELETE | /api/v1/functions/{id}                             | 493ms    | 240ms       |
| POST   | /api/v1/functions                                  | 399ms    | 210ms       |
| GET    | /api/v1/points/sortField=id&ascending=true         | 1162ms   | 720ms       |
| GET    | /api/v1/points/function/{id}/sortField=xValue&ascending=true | 512ms    | 270ms       |
| POST   | /api/v1/points                                     | 313ms    | 170ms       |
| POST   | /api/v1/points/batch                               | 469ms    | 240ms       |
| POST   | /api/v1/points/batch/search-by-ids                 | 397ms    | 220ms       |
| PUT    | /api/v1/points/{id}                                | 681ms    | 320ms       |
| DELETE | /api/v1/points/{id}                                | 639ms    | 300ms       |
| DELETE | /api/v1/points/function/{id}                       | 510ms    | 260ms       |
| DELETE | /api/v1/functions/{id}                             | 523ms    | 280ms       |