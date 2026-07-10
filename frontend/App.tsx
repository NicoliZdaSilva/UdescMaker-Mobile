import { NavigationContainer, DefaultTheme } from '@react-navigation/native';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { CatalogProvider } from './src/hooks/CatalogContext';
import { RootNavigator } from './src/navigation/RootNavigator';
import { colors } from './src/theme';

const navigationTheme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    background: colors.background,
    primary: colors.brand,
    card: colors.background,
    text: colors.text,
    border: colors.line
  }
};

export default function App() {
  return (
    <SafeAreaProvider>
      <CatalogProvider>
        <NavigationContainer theme={navigationTheme}>
          <StatusBar style="dark" />
          <RootNavigator />
        </NavigationContainer>
      </CatalogProvider>
    </SafeAreaProvider>
  );
}
