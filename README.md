# 홍시프로젝트
> 가족 일상공유 앱 프로젝트

가족을 위한 일상공유 앱

## 환경

- aws elb (docker-compose)
  * AWS ECR private repository 도커이미지 배포 
  * docker-compose, Dockerrun.aws.json을 통한 ECR이미지 배포
- postgresql
- sentry (error-log 기록)
  * GlobalExceptionHandler 공통에러 핸들러에서 sentry에 에러기록
- firebase cloud message
  * server 부분만 구현상태 (flutter 쪽 미구현)
- jwt(spring-security)
  * spring-security 이용하여 jwt 인증구현 
  * 기본 로그인 및 oauth 로그인에 jwt access token 및 refresh token 이용

## 프로젝트 개요

<<프로젝트 구조>>

- [Common] - Utility 성 공통 
           - 공통 응답객체 (CommonResponse)
- [Config] - Aws Config - FirebaseConfig(FCM)
- [Controller] 컨트롤러 
- [Service] 서비스
- [Dto] DTO
- [Exception] GlobalExceptionHandler
- [Model] postgresModel
- [Repository] JPA Repository
- [Security] JWT 인증/인가 구현

## 업데이트 내역
* 0.1.1
    * readme 추가 
* 0.0.1
    * 첫 배포 
