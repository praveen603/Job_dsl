package lib

import static com.google.common.base.Preconditions.checkState

import com.google.common.base.Charsets
import com.google.common.hash.Hashing

/*
 * This class provides static methods for Freestyle projects and pipelines.
 */
class JobDslSupport {

  /*
   * Returns a 32-bit Murmur3 hash that represents the Jenkins master on which this job is running.
   */
  def static String jenkinsTag(Binding binding) {
    String jenkinsUrl = binding.variables.get('JENKINS_URL')
    return Hashing.murmur3_32().newHasher().putString(jenkinsUrl, Charsets.UTF_8).hash().toString()
  }

  /*
   * Returns a unique VM name for each build, for use when cloning test VMs. The limit imposed by VMware for VM dataset
   * names is 42 characters. We make use of these 42 characters as follows:
   *
   *      3    DCenter prepends the string "zfs" to each dataset name.
   *
   *      1    "_" separator character (from DCenter).
   *
   *     22    The name of the Jenkins job.
   *
   *      1    "-" separator character.
   *
   *      6    The build number of the Jenkins job. For now, we support build numbers up to 999999 (six digits).
   *
   *      1    "-" separator character.
   *
   *      8    A 32-bit Murmur3 hash that can be used to store additional information to make the name unique.
   *           Currently this hash only utilizes the URL of the Jenkins master, but it could be expanded in the future
   *           to utilize additional information.
   *     --
   *     42    The limit imposed by VMware for VM dataset names.
   */
  def static String testVMName(Binding binding, String jobName) {
    checkState(jobName.length() <= 22, "job name %s must be at most 22 characters", jobName)
    return jobName + '-${BUILD_NUMBER}-' + jenkinsTag(binding)
  }

  /*
   * This function takes as input a short branch name (i.e. 'master' not 'refs/heads/master' or 'origin/master') and
   * returns true if the branch is a "shipping" branch as opposed to a temporary or project branch.
   *
   * This function should be used to restrict expensive jobs to only run automatically on "shipping" branches. For
   * example, if we have many regression testing jobs scheduled by cron strings we do not want to run every regression
   * test on every project branch on the same schedule.
   *
   * Note that scripts jobs are not required to skip creating expensive jobs on project branches, just scheduling them
   * to run automatically. A common pattern might be:
   *
   * if (isShippingBranch(branchName)) {
   *     triggers {
   *         scm("H/5 * * * *")
   *     }
   * }
   *
   * This allows the job to exist and be manually runnable for any branch, but avoids running it on every push to the
   * branch.
   */
  def static boolean isShippingBranch(String branchName) {
    if (branchName.equals("master")) {
      return true
    } else if (branchName ==~ /[0-9]\.[0-9]\/(stage|release)/) {
      return true
    } else {
      return false
    }
  }

  /*
   * Returns true if this Jenkins instance was started by a developer (as opposed to a master production instance). This
   * should be used to restrict jobs to not be automatically triggered on developer instances.
   */
  def static boolean isDeveloperJenkinsInstance() {
    return System.getenv('JENKINS_DEVELOPER') != null
  }
}
