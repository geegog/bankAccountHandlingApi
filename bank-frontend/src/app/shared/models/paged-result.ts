export interface IPagedResult<T> {
  content: Array<T>;
  totalPages: number;
  totalElements: number;
  numberOfElements: number;
  number: number;
  size: number;
  last: boolean;
  first: boolean;
}
