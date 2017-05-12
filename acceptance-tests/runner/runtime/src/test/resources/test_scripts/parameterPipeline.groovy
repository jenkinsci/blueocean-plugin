node() {
   // adds job parameters within jenkinsfile
   properties([
     parameters([
       booleanParam(
         defaultValue: false,
         description: 'Should we start the build?',
         name: 'ShouldBuild'
       ),
       choice(
           choices: 'master\ndevelopment\nfeatureXYZ\nhotfix69\nrevolution\nrefactor\nuglify',
           description: 'Which branch do you want to build?',
           name: 'Branch'),
     ])
   ])
   if (params.ShouldBuild) { print "We are going to build now the branch " + params.Branch }
   else { print "Not building anything nor branch "+ params.Branch }

}