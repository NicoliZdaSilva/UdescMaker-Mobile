module.exports = {
  preset: 'jest-expo',
  setupFilesAfterEnv: ['<rootDir>/src/test/setup.ts'],
  collectCoverageFrom: ['src/**/*.{ts,tsx}', '!src/test/**', '!src/**/*.d.ts'],
  testPathIgnorePatterns: ['/node_modules/', '/e2e/']
};
