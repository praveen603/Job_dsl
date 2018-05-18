job('seed') {
    description 'This job is used to create others jobs in automated way'
    scm {
        git{
            remote{
                url('https://github.com/praveen603/Job_dsl.git')
                branch('master')
           }
        }
    }
    triggers {
        //scm 'H/5 * * * *'
    }
    steps {

        gradle 'clean test'
        dsl {
            external 'jenkins/jobs/pipline/*.groovy'
            additionalClasspath 'src/main/groovy'
        }
    }
    publishers {
        archiveJunit 'build/test-results/**/*.xml'
    }
}
