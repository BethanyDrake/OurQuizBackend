# OurQuizBackend
Kotlin/Spring Rest Api for OurQuiz 

Frontend can be found here: https://github.com/BethanyDrake/OurQuizAndroidApp


Commands:

Run: `./mvnw spring-boot:run`

Deploy:
1. copy files over to ec2 instance:
`scp -i /personal-1.pem -r /Users/bethany/Desktop/our-quiz ec2-user@ec2-13-54-215-65.ap-southeast-2.compute.amazonaws.com:`

2. access ec2 instance:
`ssh -i ./personal-1.pem ec2-user@ec2-13-54-215-65.ap-southeast-2.compute.amazonaws.com`

3. build and run:

```
cd our-quiz
docker run -d -it --rm --name my-maven-project -v maven-repo:/root/.m2  -v "$(pwd)":/home/ec2-user/our-quiz -w /home/ec2-user/our-quiz -p 8090:8090 maven:3.3-jdk-8 mvn spring-boot:run
```
