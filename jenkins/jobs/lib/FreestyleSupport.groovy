
package lib

import javaposse.jobdsl.dsl.helpers.step.StepContext

/*
 * This class provides static methods for Freestyle projects.
 */
class FreestyleSupport {
  static String[] SCRIPT_KEEP_VARS = [
    'HOME',
    'JENKINS_URL',
    'BUILD_NUMBER',
    'BUILD_URL',
    'DCENTER_HOST',
    'JOB_NAME',
    'PATH',
    'LANG',
    'WORKSPACE',
    '__CI_OUTPUT_FILE',
    '__CI_OUTPUT_PREFIX',
  ]

  static Map<String, String> SCRIPT_SET_VARS = [
    CI_SH_LIB: 'devops-gate/lib/sh',
    CI_PY_LIB: 'devops-gate/lib/python',
    PYTHONPATH: 'devops-gate/lib/python',
  ]

  static String _SCRIPT_TERM = '__xxx_jenkins_script_terminator_xxx__'

  /*
   * Executes the given delphix python script with all the delphix
   * boilerplate:
   *  - Export the given set of parameters (env) as environment
   *    variables.
   *  - Strip the environment down to the specified parameters
   *    and SCRIPT_KEEP_VARS.
   *  - Print a pretty banner explaining what we're about to do.
   *  - Install a virtual environment for the python script to execute in
   *  - Install any requirements from requirements.txt from the directory
   *    of the python script.
   */
  def static void dlpxPython(StepContext context, String script,
          String description, Map<String, String> env) {
    def titleWidth = 80

    def scriptPath = String.format('devops-gate/scripts/%s', script)
    def requirements = String.format('%s/requirements.txt', scriptPath)
    script = String.format('%s/%s.py', scriptPath, script.replaceAll('-', '_'))
    def devpiAddress = 'http://devpi.delphix.com:3141/root/public/+simple/'
    def pathlen = script.length()
    if (pathlen % 2 == 1)
      pathlen += 1
    def spaces = ' ' * ((titleWidth - (pathlen + 2)) / 2)
    def banner = String.format('#%s\\E[1m%s\\E[0m%s#', spaces, script, spaces)

    def res = '#!/bin/bash\n'

    res += 'env - '
    for (String var : SCRIPT_KEEP_VARS) {
      res += String.format('%s="${%s}" ', var, var)
    }
    SCRIPT_SET_VARS.each { String key, String value ->
      value = value.replace('"', '\\"')
      res += String.format('%s="%s" ', key, value)
    }
    env.each { String key, String value ->
      value = value.replace('"', '\\"')
      res += String.format('%s="%s" ', key, value)
    }

    res += String.format('PYTHONPATH="$PYTHONPATH:%s" ', SCRIPT_SET_VARS["CI_PY_LIB"])

    res += String.format('/bin/bash <<%s\n', _SCRIPT_TERM)
    res += String.format('echo "%s"\n', '#' * titleWidth)
    res += String.format('echo -e "%s"\n', banner)
    res += String.format('echo "%s"\n', '#' * titleWidth)
    res += 'echo "#"\n'
    res += String.format('echo "%s" | fold -s -w %d | sed "s/^/#  /"\n', description, titleWidth - 5)
    res += 'echo "#"\n'
    env.each() { key, value ->
      res += String.format('echo -e "#  \\E[1m%s\\E[0m = %s"\n', key, value)
    }
    res += 'echo "#"\n'
    res += String.format('source "%s/common.sh"\n', SCRIPT_SET_VARS["CI_SH_LIB"])

    res += 'log_must virtualenv venv\n'
    res += 'log_must source venv/bin/activate\n'
    res += String.format('log_must python ./venv/bin/pip install -U -i %s -r %s\n', devpiAddress, requirements)
    res += 'echo "" >"${WORKSPACE}/script.out"\n'
    res += String.format('log_must python -u %s\n', script)
    res += _SCRIPT_TERM

    context.with {
      environmentVariables {
        envs([
          __CI_OUTPUT_PREFIX: '__CI_SCRIPT_1_OUTPUT___',
          __CI_OUTPUT_FILE: '${WORKSPACE}/script.out'
        ])
      }
      shell(res)
      environmentVariables { propertiesFile('${WORKSPACE}/script.out') }
    }
  }

  /*
   * Executes the given delphix shell script with all the delphix
   * boilerplate:
   *  - Export the given set of parameters (env) as environment
   *    variables.
   *  - Strip the environment down to the specified parameters
   *    and SCRIPT_KEEP_VARS.
   *  - Print a pretty banner explaining what we're about to do.
   */
  def static void dlpxShell(StepContext context, String script, String description, Map<String, String> env) {
    def titleWidth = 80
    script = String.format('scripts/%s/%s.sh', script, script)
    def pathlen = script.length()
    if (pathlen % 2 == 1)
      pathlen += 1
    def spaces = ' ' * ((titleWidth - (pathlen + 2)) / 2)
    def banner = String.format('#%s\\E[1m%s\\E[0m%s#', spaces, script, spaces)

    def res = '#!/bin/bash\n'

    res += 'env - '
    for (String var : SCRIPT_KEEP_VARS) {
      res += String.format('%s="${%s}" ', var, var)
    }
    SCRIPT_SET_VARS.each { String key, String value ->
      value = value.replace('"', '\\"')
      res += String.format('%s="%s" ', key, value)
    }
    env.each { String key, String value ->
      value = value.replace('"', '\\"')
      res += String.format('%s="%s" ', key, value)
    }

    res += String.format('/bin/bash <<%s\n', _SCRIPT_TERM)
    res += String.format('echo "%s"\n', '#' * titleWidth)
    res += String.format('echo -e "%s"\n', banner)
    res += String.format('echo "%s"\n', '#' * titleWidth)
    res += 'echo "#"\n'
    res += String.format('echo "%s" | fold -s -w %d | sed "s/^/#  /"\n', description, titleWidth - 5)
    res += 'echo "#"\n'
    env.each() { key, value ->
      res += String.format('echo -e "#  \\E[1m%s\\E[0m = %s"\n', key, value)
    }
    res += 'echo "#"\n'
    res += 'echo "" >"${WORKSPACE}/script.out"\n'
    res += String.format('devops-gate/%s\n', script)
    res += _SCRIPT_TERM

    context.with {
      environmentVariables {
        envs([
          __CI_OUTPUT_PREFIX: '__CI_SCRIPT_1_OUTPUT___',
          __CI_OUTPUT_FILE: '${WORKSPACE}/script.out'
        ])
      }
      shell(res)
      environmentVariables { propertiesFile('${WORKSPACE}/script.out') }
    }
  }
}
