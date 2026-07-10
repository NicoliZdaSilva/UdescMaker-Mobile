import { render } from '@testing-library/react-native';
import { Text } from 'react-native';

import { AsyncState } from './AsyncState';

describe('AsyncState', () => {
  it('renderiza loading', () => {
    expect(render(<AsyncState loading />).getByTestId('loading-state')).toBeTruthy();
  });

  it('renderiza erro recuperável', () => {
    const screen = render(<AsyncState error={new Error('Sem conexão')} loading={false} onRetry={jest.fn()} />);
    expect(screen.getByTestId('error-state')).toBeTruthy();
    expect(screen.getByText('Tentar novamente')).toBeTruthy();
  });

  it('renderiza vazio e conteúdo', () => {
    expect(render(<AsyncState empty loading={false} />).getByTestId('empty-state')).toBeTruthy();
    expect(
      render(
        <AsyncState loading={false}>
          <Text>Conteúdo</Text>
        </AsyncState>
      ).getByText('Conteúdo')
    ).toBeTruthy();
  });
});
