/* eslint-disable quotes,quote-props,comma-dangle */
export const pipelines = [
    {
        '_class': 'some.class',
        '_links': {
            'self': {
                '_class': 'io.jenkins.blueocean.rest.hal.Link',
                'href': '/blue/rest/organizations/jenkins/pipelines/morebeers/'
            },
        },
        'displayName': 'moreBeers',
        'name': 'morebeers',
        'organization': 'jenkins',
        'weatherScore': 0,
        'branchNames': ['master'],
        'numberOfFailingBranches': 1,
        'numberOfFailingPullRequests': 0,
        'numberOfSuccessfulBranches': 0,
        'numberOfSuccessfulPullRequests': 0,
        'totalNumberOfBranches': 1,
        'totalNumberOfPullRequests': 0
    },
    {
        '_class': 'some.class',
        '_links': {
            'self': {
                '_class': 'io.jenkins.blueocean.rest.hal.Link',
                'href': '/blue/rest/organizations/jenkins/pipelines/beers/'
            },
        },
        'displayName': 'beers',
        'name': 'beers',
        'organization': 'jenkins',
        'weatherScore': 0
    }
];
