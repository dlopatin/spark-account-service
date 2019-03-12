Spark framework based REST app for simple money transfers between accounts.
Runs on default Spark port: 4567

Money stored in long and represents cents, pennies, etc.\
Request validation performed for transfer.

Routes:
* GET `/api/v1/account/:id` - get account with given id  
    Curl example: `curl -i http://localhost:4567/api/v1/account/1`

    Response:
    * Successful:
        ```
        HTTP/1.1 200 OK
        Content-Type: application/json
        
        {
            "id": <int>,
            "currency": "<string>",
            "balance": <long>
        }
        ```
    * Account not found:
        ```
        HTTP/1.1 404 Not Found
        Content-Type: application/json
        
        {
            "errors": [{
                "code": "ACCOUNT_NOT_FOUND",
                "detail": "Account by id=10 not found"
            }]
        }
        ```
* POST `/api/v1/account` - create new account.  
    Curl example: `curl -X POST -i http://localhost:4567/api/v1/account -d '{"currency":"GBP", "balance":"10"}'`
    
    Request format:
    ```json
    {
        "currency": "<string>",
        "balance":  "<long>"
    }
    ```
    Currency enum one of: `GBP`, `USD`, `EUR`, `RUB`;
    
    Response:
    ```
    HTTP/1.1 201 Created
    Content-Type: application/json
      
    {"id": <int>}
    ```
* POST `/api/v1/account/transfer` - make a money transfer between accounts.  
    Curl example: `curl -X POST -i http://localhost:4567/api/v1/account/transfer -d '{"accountFrom":"1", "accountTo":"2", "amount":"1000", "operationId":"1"}'`
    
    Request format:
    ```json
    {
      "accountFrom": "<int>",
      "accountTo":   "<int>",
      "amount":      "<long>",
      "operationId": "<int>"
    }
    ```
    `operationId` - id of operation to avoid unwanted transfer duplication
    
    Response:
    * Successful:
        ```
        HTTP/1.1 200 OK
        Content-Type: application/json
        
        {"message":"ok"}
        ```
    * Error
        * Validation error:
            ```
            HTTP/1.1 400 Bad Request
            Content-Type: application/json
                        
            {
                "errors": [{
                    "code": "VALIDATION_ERROR",
                    "detail": "To account must be positive"
                }]
            }
            ```
        * Operation error:
            ```
            HTTP/1.1 400 Bad Request
            Content-Type: application/json
            
            {
                "errors": [{
                    "code": "INSUFFICIENT_BALANCE",
                    "detail": null
                }]
            }
            ```