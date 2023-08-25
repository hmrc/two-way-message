# two-way-message

## Rendered message API

| Path                                   | Supported Methods | Description                                                    |
|----------------------------------------|-------------------|----------------------------------------------------------------|
| `/messages/:messageId/content`         | GET               | return a rendered (HTML) version of the message for a customer |

Return Status Codes:
- **200** (OK) - body contain HTML of the rendered conversation

## Note on Integration Tests

For integration tests to run successfully you need the following apps run by sm externally:

`sm2 --start DC_TWO_WAY_MESSAGE_IT`


## Deprecation notes
This service used to be able to create two-way messages between a customer and a HMRC advisor
however this functionality was deprecated.

If you still need to support rendering these types of messages and need to set up test data set for
your manual or automated testing then you can find example JSON files of messages in `it/resources`.
These messages need to be inserted into the `message` collection in the `message` database.

## License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
