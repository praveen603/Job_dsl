job('seed') {
    description 'This job is used to create others jobs in automated way'
    scm {
        git{
            remote{
                url('ssh://git@172.16.10.28:2222/devops.git')
                branch('master')
                credentials('e0ce4893-3c53-4fc1-817e-cae0c0074bc2')
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