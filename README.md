# OutWait
OutWait is a platform to manage waiting times in facilities like doctor offices or public departments. Queues of waiting clients can be managed through an android interface and clients can register using a private code to look up their remaining waiting time on their mobile phone and get notified when they are next.

This project was created for the computer science lecture "PSE" at the Karlsruhe Institute of Technology.

## Getting started
```
git clone https://github.com/OutWait/OutWait.git
cd outwait

# App
cd app/OutWait
./gradlew build

cd ../..

# Server (use gradle 6 if you encounter problems)
cd server
gradle build
# To start the server: gradle run

```

## Testing
```
cd app/OutWait
# Unit tests (app)
./gradlew test
# Android tests
./gradlew connectedAndroidTest
cd ../..

# Unit tests (server)
cd server
gradle test
```
Sometimes specific android tests fail when executed sequentially. If this happens, just run the tests individually in Android Studio.

To pass all server tests, a running mysql instance with the appropriate test database is required.
