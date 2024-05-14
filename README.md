# README

## 임시 AIMS REST API 주소

- [http://158.247.208.28:3000/getLabelList](http://158.247.208.28:3000/getLabelList)

## 배치

- 내장 배치 사용
- ScheduledTaskRegistrar 를 사용하여 스캐쥴 정보를 등록
- LabelSchedule => LabelService 호출 ( mybatis Mapper 를 사용하여 DB CRUD 수행 )

## 웹

- 엑셀 다운로드
- 의존성
  - spring-boot-starter-web : 웹

## 공통

- 의존성
  - lombok : vo 객체 생성
  - mariadb-java-client : maria DB연결
  - mybatis-spring-boot-starter : mybatis
  - spring-boot-devtools : hot deploy 외

## 미사용

- 의존성
  - spring-boot-starter-mail : 메일발송

## 참조링크

- [Enabling Cross Origin Requests for a RESTful Web Service](https://spring.io/guides/gs/rest-service-cors)
- [[java] Quartz를 이용해 Spring에서 Scheduling하기](https://wouldyou.tistory.com/94#google_vignette)
- [[Springboot] 설정 파일에서 값 가져오는 방법](https://velog.io/@haerong22/Springboot-%EC%84%A4%EC%A0%95-%ED%8C%8C%EC%9D%BC%EC%97%90%EC%84%9C-%EA%B0%92-%EA%B0%80%EC%A0%B8%EC%98%A4%EB%8A%94-%EB%B0%A9%EB%B2%95)
- [VSCODE에서 환경변수로 실행하기 (feat. spring profile 구분 실행)](https://velog.io/@ililil9482/VSCODE%EC%97%90%EC%84%9C-%ED%99%98%EA%B2%BD%EB%B3%80%EC%88%98%EB%A1%9C-%EC%8B%A4%ED%96%89%ED%95%98%EA%B8%B0-feat.-spring-profile-%EA%B5%AC%EB%B6%84-%EC%8B%A4%ED%96%89)
- [[Git] 특정 파일 혹은 폴더 무시하기 (.gitignore)](https://sunhyeokchoe.github.io/posts/Ignoring-Files/)
- [[Spring] Mybatis 연동 시 오류 해결방법](https://cceeun.tistory.com/83)
