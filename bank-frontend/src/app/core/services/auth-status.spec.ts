import { TestBed } from '@angular/core/testing';

import { AuthStatus } from './auth-status';

describe('AuthStatus', () => {
  let service: AuthStatus;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthStatus);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
