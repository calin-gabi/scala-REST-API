# Scala AKKA REST API project

## Instalation

Clone the repository in your workspace:
    
    $> git clone https://github.com/gabriel-montagne/scala-REST-API.git
    $> cd scala-REST-API
    
Run docker-compose:

    $> docker-compose up

If you want to rebuild the docker image run:

    $> docker-compose up --build
    
## Tests

Connect with docker container in terminal:

    $> docker exec -it scalarestapi_web_1 /bin/bash
    
Run tests:

    $> sbt test