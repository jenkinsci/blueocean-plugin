node {
    stage("two minus one"){
        def branchInput = input id: 'CustomIdHere', message: 'this is a message to user', ok: 'Go ahead', parameters: [
                booleanParam(defaultValue: true, description: 'yes or no', name: 'thisIsBool'),
                choice(choices: 'radio1\nradio2\nradio3\nradio4', description: 'this is choice Radio description', name: 'This is choice Radio'),
choice(choices: 'drop1\ndrop2\ndrop3\ndrop4\ndrop5\ndrop6\ndrop7', description: 'this is choice Dropdown description', name: 'This is choice dropdown'),
                text(defaultValue: 'default', description: 'Long text goes here', name: 'This is a multi line string'),
                string(defaultValue: 'yeah', description: 'string parameter desc', name: 'this is a string parmeter'),
                password(defaultValue: 'secret', description: 'password param desc', name: 'password param')
            ]
        echo "BRANCH INPUT: ${branchInput}"
    }

}