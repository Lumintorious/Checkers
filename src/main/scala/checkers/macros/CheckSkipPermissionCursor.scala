package checkers.macros

import io.circe.HCursor
import io.circe.CursorOp
import io.circe.Json
import io.circe.cursor.ArrayCursor
import io.circe.ACursor

class CheckSkipPermissionCursor(json: Json)
extends HCursor(null, null){
  private val underlying: HCursor = HCursor.fromJson(json)
  
  def value = json
  override def index: Option[Int] = None
  override def key: Option[String] = None

  def replace(newValue: Json, cursor: HCursor, op: CursorOp): HCursor =
    this
    
  def addOp(cursor: HCursor, op: CursorOp): HCursor =
    this

  def up: ACursor = underlying
  def delete: ACursor = fail(CursorOp.DeleteGoParent)

  def left: ACursor = fail(CursorOp.MoveLeft)
  def right: ACursor = fail(CursorOp.MoveRight)

  def field(k: String): ACursor = fail(CursorOp.Field(k))
}
