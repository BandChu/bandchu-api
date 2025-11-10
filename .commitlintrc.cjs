const TYPES = '(feat|fix|refactor|test|build|ci|docs|perf)';

const headerPattern = new RegExp(
    String.raw`^(?:\[[A-Z]+-\d+\]\s)?${TYPES}:\s[a-z][^.]*$`
);

module.exports = {
    extends: ['@commitlint/config-conventional'],
    parserPreset: {
        parserOpts: {
            headerPattern: /^(\[[A-Z]+-\d+]\s)?(\w*)(?:\((.*)\))?: (.*)$/,
            headerCorrespondence: ['jira', 'type', 'scope', 'subject'],
        },
    },
    rules: {
        'type-empty': [2, 'never'],
        'type-enum': [
            2,
            'always',
            ['feat', 'fix', 'refactor', 'test', 'build', 'ci', 'docs', 'perf'],
        ],

        'subject-empty': [2, 'never'],
        'subject-case': [2, 'never', []],
        'subject-full-stop': [2, 'never', '.'],

        'scope-case': [2, 'never', []],

        'body-leading-blank': [1, 'always'],
        'body-max-line-length': [2, 'always', 100],

        'footer-leading-blank': [1, 'always'],
        'footer-max-line-length': [2, 'always', 100]
    },
};
