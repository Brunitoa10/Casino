import type { ReactNode } from 'react';
import { TopBar } from './TopBar';
import { RoundFeed } from './RoundFeed';
import styles from './Layout.module.css';

export function AppShell({ nav, children }: { nav: ReactNode; children: ReactNode }) {
  return (
    <div className={styles.shell}>
      <TopBar />
      {nav}
      <main className={styles.main}>{children}</main>
      <RoundFeed />
    </div>
  );
}
