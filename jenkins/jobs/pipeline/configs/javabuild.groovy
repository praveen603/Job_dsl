String basePath = 'test-Demo'


folder(basePath) {
    description 'his is fist job to build and deploy.'
}

job("$basePath/job-build") {
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
        scm 'H/5 * * * *'
    }
    steps {
        gradle 'assemble'
    }
}

job("$basePath/job-deploy") {
    parameters {
        stringParam('RemoteHost', '172.16.10.28', 'This is test job using dsl groovy script.')

    }
    steps {
        shell 'scp ROOT.war root@$RemoteHost; service tomcat restart'
    }
}