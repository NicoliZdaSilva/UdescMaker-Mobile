import { fireEvent, render } from '@testing-library/react-native';

import { DynamicTextList } from './DynamicTextList';

describe('DynamicTextList', () => {
  it('permite editar, adicionar e remover itens', () => {
    const onChange = jest.fn();
    const onAdd = jest.fn();
    const onRemove = jest.fn();
    const screen = render(
      <DynamicTextList
        label="Materiais"
        onAdd={onAdd}
        onChange={onChange}
        onRemove={onRemove}
        values={[{ id: '1', valor: 'Papel' }]}
      />
    );

    fireEvent.changeText(screen.getByLabelText('Materiais 1'), 'Papelão');
    fireEvent.press(screen.getByText('Adicionar item'));
    fireEvent.press(screen.getByLabelText('Remover item 1 de Materiais'));

    expect(onChange).toHaveBeenCalledWith(0, 'Papelão');
    expect(onAdd).toHaveBeenCalledTimes(1);
    expect(onRemove).toHaveBeenCalledWith(0);
  });
});
